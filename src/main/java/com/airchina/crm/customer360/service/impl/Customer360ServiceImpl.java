package com.airchina.crm.customer360.service.impl;

import com.airchina.crm.common.config.RedisCacheHelper;
import com.airchina.crm.common.enums.MemberTier;
import com.airchina.crm.customer360.service.Customer360Service;
import com.airchina.crm.customer360.vo.Customer360VO;
import com.airchina.crm.member.entity.Member;
import com.airchina.crm.member.service.MemberService;
import com.airchina.crm.miles.service.MilesService;
import com.airchina.crm.miles.vo.MilesBalanceVO;
import com.airchina.crm.workorder.entity.WorkOrder;
import com.airchina.crm.workorder.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 客户360视图 - 门面模式（Facade）
 *
 * 聚合多个模块的数据，对外提供统一查询接口：
 * 1. 会员信息 ← memberService
 * 2. 积分余额 ← milesService
 * 3. 活跃工单 ← workorderService
 *
 * 缓存策略：5分钟 TTL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Customer360ServiceImpl implements Customer360Service {

    private final MemberService memberService;
    private final MilesService milesService;
    private final WorkOrderService workOrderService;
    private final RedisCacheHelper redisCacheHelper;

    private static final String CACHE_KEY_PREFIX = "360:summary:";
    private static final long CACHE_TTL_MINUTES = 5;

    @Override
    public Customer360VO getCustomer360(Long memberId) {
        String cacheKey = CACHE_KEY_PREFIX + memberId;

        // 1. 先查缓存（Redis 不可用时降级跳过）
        try {
            Object cached = redisCacheHelper.get(cacheKey);
            if (cached instanceof Customer360VO) {
                log.debug("360视图缓存命中：memberId={}", memberId);
                return (Customer360VO) cached;
            }
        } catch (Exception e) {
            log.warn("Redis 缓存读取失败，降级查库：{}", e.getMessage());
        }

        // 2. 缓存未命中，聚合查询
        log.debug("360视图执行聚合查询：memberId={}", memberId);
        Customer360VO vo = buildCustomer360(memberId);

        // 3. 写入缓存（Redis 不可用时静默失败）
        try {
            redisCacheHelper.set(cacheKey, vo, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 缓存写入失败，跳过缓存：{}", e.getMessage());
        }

        return vo;
    }

    private Customer360VO buildCustomer360(Long memberId) {
        Customer360VO vo = new Customer360VO();

        // 会员信息
        Member member = memberService.getMemberById(memberId);
        Customer360VO.MemberInfo memberInfo = new Customer360VO.MemberInfo();
        memberInfo.setMemberId(member.getMemberId());
        memberInfo.setMemberNo(member.getMemberNo());
        memberInfo.setName(member.getName());
        memberInfo.setTier(member.getTier());
        memberInfo.setTierDesc(MemberTier.fromCode(member.getTier()).getDesc());
        memberInfo.setQualifyingMiles(member.getQualifyingMiles());
        memberInfo.setRedeemableMiles(member.getRedeemableMiles());
        memberInfo.setMobile(member.getMobile());
        vo.setMember(memberInfo);

        // 积分余额
        try {
            MilesBalanceVO milesBalance = milesService.queryBalance(memberId);
            vo.setMiles(milesBalance);
        } catch (Exception e) {
            log.warn("查询积分余额失败：memberId={}", memberId, e);
        }

        // 活跃工单
        try {
            List<WorkOrder> activeOrders = workOrderService.getActiveOrdersByMember(memberId);
            vo.setActiveOrders(activeOrders);
        } catch (Exception e) {
            log.warn("查询活跃工单失败：memberId={}", memberId, e);
        }

        vo.setLoadedAt(LocalDateTime.now());
        return vo;
    }
}
