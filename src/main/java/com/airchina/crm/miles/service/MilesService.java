package com.airchina.crm.miles.service;

import com.airchina.crm.miles.dto.MilesDeductDTO;
import com.airchina.crm.miles.dto.MilesEarnDTO;
import com.airchina.crm.miles.entity.MilesTransaction;
import com.airchina.crm.miles.vo.MilesBalanceVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 积分服务接口
 */
public interface MilesService extends IService<MilesTransaction> {

    /**
     * 查询积分余额
     */
    MilesBalanceVO queryBalance(Long memberId);

    /**
     * 查询积分流水（最近N条）
     */
    List<MilesTransaction> queryRecentTransactions(Long memberId, int limit);

    /**
     * 积分扣减（乐观锁，最多重试3次）
     */
    void deduct(MilesDeductDTO dto);

    /**
     * 积分发放
     */
    void earn(MilesEarnDTO dto);
}
