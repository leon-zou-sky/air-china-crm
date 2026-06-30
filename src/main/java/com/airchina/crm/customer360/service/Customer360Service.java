package com.airchina.crm.customer360.service;

import com.airchina.crm.customer360.vo.Customer360VO;

/**
 * 客户360视图服务接口
 */
public interface Customer360Service {

    /**
     * 获取客户360视图（聚合多模块数据）
     */
    Customer360VO getCustomer360(Long memberId);
}
