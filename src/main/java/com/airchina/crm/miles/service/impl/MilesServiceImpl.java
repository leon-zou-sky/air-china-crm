package com.airchina.crm.miles.service.impl;

import com.airchina.crm.common.config.RedisCacheHelper;
import com.airchina.crm.common.enums.MilesTxType;
import com.airchina.crm.common.exception.BizException;
import com.airchina.crm.common.result.ResultCode;
import com.airchina.crm.miles.dto.MilesDeductDTO;
import com.airchina.crm.miles.dto.MilesEarnDTO;
import com.airchina.crm.miles.entity.MilesAccount;
import com.airchina.crm.miles.entity.MilesTransaction;
import com.airchina.crm.miles.mapper.MilesAccountMapper;
import com.airchina.crm.miles.mapper.MilesTransactionMapper;
import com.airchina.crm.miles.service.MilesService;
import com.airchina.crm.miles.vo.MilesBalanceVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 积分服务实现
 *
 * 核心设计：
 * 1. 乐观锁扣减（version 字段 CAS）
 * 2. 失败重试最多3次
 * 3. 扣减成功后写流水 + 清除缓存
 * 4. 查询走 Redis 缓存（Cache Aside）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MilesServiceImpl extends ServiceImpl<MilesTransactionMapper, MilesTransaction> implements MilesService {

    private final MilesAccountMapper milesAccountMapper;
    private final RedisCacheHelper redisCacheHelper;

    private static final int MAX_RETRY = 3;
    private static final String CACHE_KEY_BALANCE = "miles:balance:";
    private static final String CACHE_KEY_TX_RECENT = "miles:tx:recent:";
    private static final long CACHE_TTL_BALANCE = 10;    // 10分钟
    private static final long CACHE_TTL_TX = 5;          // 5分钟

    /**
     * 查询积分余额（带缓存）
     */
    @Override
    public MilesBalanceVO queryBalance(Long memberId) {
        // 1. 先查缓存
        String cacheKey = CACHE_KEY_BALANCE + memberId;
        try {
            MilesBalanceVO cached = redisCacheHelper.get(cacheKey, MilesBalanceVO.class);
            if (cached != null) {
                log.debug("积分余额缓存命中：memberId={}", memberId);
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis 读取失败，降级查库：{}", e.getMessage());
        }

        // 2. 缓存未命中，查库
        MilesAccount account = milesAccountMapper.selectById(memberId);
        if (account == null) {
            throw new BizException("积分账户不存在");
        }
        MilesBalanceVO vo = toBalanceVO(account);

        // 3. 写入缓存
        try {
            redisCacheHelper.set(cacheKey, vo, CACHE_TTL_BALANCE, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 写入失败：{}", e.getMessage());
        }

        return vo;
    }

    /**
     * 查询最近积分流水（带缓存）
     */
    @Override
    public List<MilesTransaction> queryRecentTransactions(Long memberId, int limit) {
        String cacheKey = CACHE_KEY_TX_RECENT + memberId + ":" + limit;
        try {
            Object cached = redisCacheHelper.get(cacheKey);
            if (cached instanceof List) {
                log.debug("积分流水缓存命中：memberId={}", memberId);
                return (List<MilesTransaction>) cached;
            }
        } catch (Exception e) {
            log.warn("Redis 读取失败，降级查库：{}", e.getMessage());
        }

        LambdaQueryWrapper<MilesTransaction> wrapper = new LambdaQueryWrapper<MilesTransaction>()
                .eq(MilesTransaction::getMemberId, memberId)
                .orderByDesc(MilesTransaction::getCreatedAt)
                .last("LIMIT " + limit);
        List<MilesTransaction> list = list(wrapper);

        try {
            redisCacheHelper.set(cacheKey, list, CACHE_TTL_TX, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 写入失败：{}", e.getMessage());
        }

        return list;
    }

    /**
     * 积分扣减（核心：乐观锁 + 重试 + 清缓存）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(MilesDeductDTO dto) {
        Long memberId = dto.getMemberId();
        int miles = dto.getMiles();

        // 1. 查询积分账户
        MilesAccount account = milesAccountMapper.selectById(memberId);
        if (account == null) {
            throw new BizException("积分账户不存在");
        }

        // 2. 余额校验
        if (account.getBalance() < miles) {
            throw new BizException(ResultCode.MILES_INSUFFICIENT);
        }

        // 3. 乐观锁扣减（带重试）
        int affected = 0;
        for (int i = 0; i < MAX_RETRY; i++) {
            affected = milesAccountMapper.deductMiles(memberId, miles, account.getVersion());
            if (affected > 0) {
                break; // 扣减成功
            }
            // 版本冲突，重新查询最新版本
            log.warn("积分扣减乐观锁冲突，重试第{}次，memberId={}", i + 1, memberId);
            account = milesAccountMapper.selectById(memberId);
            if (account == null || account.getBalance() < miles) {
                throw new BizException(ResultCode.MILES_INSUFFICIENT);
            }
        }

        if (affected == 0) {
            throw new BizException(ResultCode.MILES_VERSION_CONFLICT);
        }

        // 4. 重新查询扣减后的余额
        account = milesAccountMapper.selectById(memberId);

        // 5. 写积分流水
        MilesTransaction tx = new MilesTransaction();
        tx.setMemberId(memberId);
        tx.setTxType(MilesTxType.REDEEM.getCode());
        tx.setMiles(-miles);
        tx.setBalanceAfter(account.getBalance());
        tx.setSource(dto.getSource());
        tx.setReferenceId(dto.getReferenceId());
        tx.setDescription(dto.getDescription());
        tx.setOperator(dto.getOperator());
        save(tx);

        // 6. 清除缓存（下次读取时重新加载）
        evictBalanceCache(memberId);

        log.info("积分扣减成功：memberId={}, miles={}, balanceAfter={}", memberId, miles, account.getBalance());
    }

    /**
     * 积分发放（清缓存）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void earn(MilesEarnDTO dto) {
        Long memberId = dto.getMemberId();
        int miles = dto.getMiles();

        // 1. 累积积分
        int affected = milesAccountMapper.earnMiles(memberId, miles);
        if (affected == 0) {
            throw new BizException("积分账户不存在");
        }

        // 2. 查询最新余额
        MilesAccount account = milesAccountMapper.selectById(memberId);

        // 3. 写流水
        MilesTransaction tx = new MilesTransaction();
        tx.setMemberId(memberId);
        tx.setTxType(MilesTxType.EARN.getCode());
        tx.setMiles(miles);
        tx.setBalanceAfter(account.getBalance());
        tx.setSource(dto.getSource());
        tx.setReferenceId(dto.getReferenceId());
        tx.setDescription(dto.getDescription());
        tx.setOperator(dto.getOperator());
        save(tx);

        // 4. 清除缓存
        evictBalanceCache(memberId);

        log.info("积分发放成功：memberId={}, miles={}, balanceAfter={}", memberId, miles, account.getBalance());
    }

    /**
     * 清除积分余额缓存
     */
    private void evictBalanceCache(Long memberId) {
        try {
            redisCacheHelper.delete(CACHE_KEY_BALANCE + memberId);
            redisCacheHelper.delete(CACHE_KEY_TX_RECENT + memberId + ":20");
            log.debug("积分缓存已清除：memberId={}", memberId);
        } catch (Exception e) {
            log.warn("缓存清除失败：{}", e.getMessage());
        }
    }

    private MilesBalanceVO toBalanceVO(MilesAccount account) {
        MilesBalanceVO vo = new MilesBalanceVO();
        vo.setMemberId(account.getMemberId());
        vo.setBalance(account.getBalance());
        vo.setFrozen(account.getFrozen());
        vo.setTotalEarned(account.getTotalEarned());
        vo.setTotalRedeemed(account.getTotalRedeemed());
        vo.setTotalExpired(account.getTotalExpired());
        vo.setVersion(account.getVersion());
        return vo;
    }
}
