package com.airchina.crm.workorder.controller;

import com.airchina.crm.common.result.Result;
import com.airchina.crm.workorder.dto.WorkOrderCreateDTO;
import com.airchina.crm.workorder.entity.WorkOrder;
import com.airchina.crm.workorder.entity.WorkOrderFlow;
import com.airchina.crm.workorder.service.WorkOrderService;
import com.airchina.crm.workorder.service.SlaMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 工单接口
 */
@RestController
@RequestMapping("/api/workorder")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final SlaMonitorService slaMonitorService;

    /**
     * 创建工单
     */
    @PostMapping
    public Result<WorkOrder> create(@Valid @RequestBody WorkOrderCreateDTO dto) {
        return Result.ok(workOrderService.createOrder(dto));
    }

    /**
     * 查询工单详情
     */
    @GetMapping("/{orderId}")
    public Result<WorkOrder> detail(@PathVariable Long orderId) {
        return Result.ok(workOrderService.getOrderDetail(orderId));
    }

    /**
     * 指派工单
     */
    @PostMapping("/{orderId}/assign")
    public Result<Void> assign(@PathVariable Long orderId,
                               @RequestParam String assignedTo,
                               @RequestParam String operator) {
        workOrderService.assignOrder(orderId, assignedTo, operator);
        return Result.ok();
    }

    /**
     * 开始处理
     */
    @PostMapping("/{orderId}/start")
    public Result<Void> start(@PathVariable Long orderId, @RequestParam String operator) {
        workOrderService.startProcessing(orderId, operator);
        return Result.ok();
    }

    /**
     * 完成工单
     */
    @PostMapping("/{orderId}/complete")
    public Result<Void> complete(@PathVariable Long orderId, @RequestParam String operator) {
        workOrderService.completeOrder(orderId, operator);
        return Result.ok();
    }

    /**
     * 关闭工单
     */
    @PostMapping("/{orderId}/close")
    public Result<Void> close(@PathVariable Long orderId,
                              @RequestParam String operator,
                              @RequestParam(required = false) String remark) {
        workOrderService.closeOrder(orderId, operator, remark);
        return Result.ok();
    }

    /**
     * 查询流转记录
     */
    @GetMapping("/{orderId}/flows")
    public Result<List<WorkOrderFlow>> flows(@PathVariable Long orderId) {
        return Result.ok(workOrderService.getFlowHistory(orderId));
    }

    /**
     * 查询会员活跃工单
     */
    @GetMapping("/member/{memberId}/active")
    public Result<List<WorkOrder>> activeOrders(@PathVariable Long memberId) {
        return Result.ok(workOrderService.getActiveOrdersByMember(memberId));
    }

    // ========== SLA 监控接口 ==========

    /**
     * 查询即将超时的工单（剩余 ≤ 30分钟）
     */
    @GetMapping("/sla/at-risk")
    public Result<List<WorkOrder>> slaAtRisk() {
        return Result.ok(slaMonitorService.listAtRiskOrders());
    }

    /**
     * 查询已超时的工单
     */
    @GetMapping("/sla/breached")
    public Result<List<WorkOrder>> slaBreached() {
        return Result.ok(slaMonitorService.listBreachedOrders());
    }

    /**
     * 查询所有 SLA 异常工单（即将超时 + 已超时）
     */
    @GetMapping("/sla/abnormal")
    public Result<List<WorkOrder>> slaAbnormal() {
        return Result.ok(slaMonitorService.listSlaAbnormalOrders());
    }
}
