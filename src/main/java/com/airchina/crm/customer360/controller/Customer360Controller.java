package com.airchina.crm.customer360.controller;

import com.airchina.crm.common.result.Result;
import com.airchina.crm.customer360.service.Customer360Service;
import com.airchina.crm.customer360.vo.Customer360VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 客户360视图接口
 */
@Tag(name = "客户360视图", description = "聚合会员信息、积分余额、活跃工单一站式查询")
@RestController
@RequestMapping("/api/customer360")
@RequiredArgsConstructor
public class Customer360Controller {

    private final Customer360Service customer360Service;

    @Operation(summary = "获取客户360视图", description = "聚合查询会员信息、积分余额、近期流水、活跃工单（缓存5分钟）")
    @GetMapping("/{memberId}")
    public Result<Customer360VO> getCustomer360(
            @Parameter(description = "会员ID", required = true, example = "1")
            @PathVariable Long memberId) {
        return Result.ok(customer360Service.getCustomer360(memberId));
    }
}
