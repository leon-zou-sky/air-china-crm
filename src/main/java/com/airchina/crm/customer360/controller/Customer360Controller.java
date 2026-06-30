package com.airchina.crm.customer360.controller;

import com.airchina.crm.common.result.Result;
import com.airchina.crm.customer360.service.Customer360Service;
import com.airchina.crm.customer360.vo.Customer360VO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 客户360视图接口
 */
@RestController
@RequestMapping("/api/customer360")
@RequiredArgsConstructor
public class Customer360Controller {

    private final Customer360Service customer360Service;

    /**
     * 获取客户360视图（聚合数据）
     *
     * 响应时间目标：
     *   缓存命中：<5ms
     *   缓存未命中：50~100ms
     */
    @GetMapping("/{memberId}")
    public Result<Customer360VO> getCustomer360(@PathVariable Long memberId) {
        return Result.ok(customer360Service.getCustomer360(memberId));
    }
}
