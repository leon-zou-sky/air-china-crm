package com.airchina.crm.workorder.controller;

import com.airchina.crm.common.result.Result;
import com.airchina.crm.workorder.dto.WorkOrderCreateDTO;
import com.airchina.crm.workorder.entity.WorkOrder;
import com.airchina.crm.workorder.entity.WorkOrderFlow;
import com.airchina.crm.workorder.service.WorkOrderService;
import com.airchina.crm.workorder.service.SlaMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 工单接口
 */
@Tag(name = "工单管理", description = "工单创建、状态流转、SLA监控")
@RestController
@RequestMapping("/api/workorder")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final SlaMonitorService slaMonitorService;

    @Operation(summary = "创建工单", description = "创建服务工单（休息室/轮椅/接送机等）")
    @PostMapping
    public Result<WorkOrder> create(@Valid @RequestBody WorkOrderCreateDTO dto) {
        return Result.ok(workOrderService.createOrder(dto));
    }

    @Operation(summary = "查询工单详情", description = "根据工单ID查询工单详细信息")
    @GetMapping("/{orderId}")
    public Result<WorkOrder> detail(
            @Parameter(description = "工单ID", required = true, example = "1")
            @PathVariable Long orderId) {
        return Result.ok(workOrderService.getOrderDetail(orderId));
    }

    @Operation(summary = "指派工单", description = "将工单指派给地服人员")
    @PostMapping("/{orderId}/assign")
    public Result<Void> assign(
            @Parameter(description = "工单ID", required = true, example = "1")
            @PathVariable Long orderId,
            @Parameter(description = "指派给（地服工号）", required = true, example = "AGENT001")
            @RequestParam String assignedTo,
            @Parameter(description = "操作人", required = true, example = "admin")
            @RequestParam String operator) {
        workOrderService.assignOrder(orderId, assignedTo, operator);
        return Result.ok();
    }

    @Operation(summary = "开始处理", description = "地服开始处理工单")
    @PostMapping("/{orderId}/start")
    public Result<Void> start(
            @Parameter(description = "工单ID", required = true, example = "1")
            @PathVariable Long orderId,
            @Parameter(description = "操作人", required = true, example = "AGENT001")
            @RequestParam String operator) {
        workOrderService.startProcessing(orderId, operator);
        return Result.ok();
    }

    @Operation(summary = "完成工单", description = "标记工单服务完成")
    @PostMapping("/{orderId}/complete")
    public Result<Void> complete(
            @Parameter(description = "工单ID", required = true, example = "1")
            @PathVariable Long orderId,
            @Parameter(description = "操作人", required = true, example = "AGENT001")
            @RequestParam String operator) {
        workOrderService.completeOrder(orderId, operator);
        return Result.ok();
    }

    @Operation(summary = "关闭工单", description = "关闭工单（归档）")
    @PostMapping("/{orderId}/close")
    public Result<Void> close(
            @Parameter(description = "工单ID", required = true, example = "1")
            @PathVariable Long orderId,
            @Parameter(description = "操作人", required = true, example = "admin")
            @RequestParam String operator,
            @Parameter(description = "备注")
            @RequestParam(required = false) String remark) {
        workOrderService.closeOrder(orderId, operator, remark);
        return Result.ok();
    }

    @Operation(summary = "查询流转记录", description = "查询工单的状态变更历史")
    @GetMapping("/{orderId}/flows")
    public Result<List<WorkOrderFlow>> flows(
            @Parameter(description = "工单ID", required = true, example = "1")
            @PathVariable Long orderId) {
        return Result.ok(workOrderService.getFlowHistory(orderId));
    }

    @Operation(summary = "查询会员活跃工单", description = "查询指定会员的所有活跃工单")
    @GetMapping("/member/{memberId}/active")
    public Result<List<WorkOrder>> activeOrders(
            @Parameter(description = "会员ID", required = true, example = "1")
            @PathVariable Long memberId) {
        return Result.ok(workOrderService.getActiveOrdersByMember(memberId));
    }

    // ========== SLA 监控接口 ==========

    @Operation(summary = "查询即将超时工单", description = "查询SLA剩余≤30分钟的工单")
    @GetMapping("/sla/at-risk")
    public Result<List<WorkOrder>> slaAtRisk() {
        return Result.ok(slaMonitorService.listAtRiskOrders());
    }

    @Operation(summary = "查询已超时工单", description = "查询SLA已超时的工单")
    @GetMapping("/sla/breached")
    public Result<List<WorkOrder>> slaBreached() {
        return Result.ok(slaMonitorService.listBreachedOrders());
    }

    @Operation(summary = "查询SLA异常工单", description = "查询所有SLA异常工单（即将超时+已超时）")
    @GetMapping("/sla/abnormal")
    public Result<List<WorkOrder>> slaAbnormal() {
        return Result.ok(slaMonitorService.listSlaAbnormalOrders());
    }
}
