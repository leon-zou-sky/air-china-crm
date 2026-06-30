package com.airchina.crm.workorder.service;

import com.airchina.crm.common.enums.WorkOrderStatus;
import com.airchina.crm.common.exception.BizException;
import com.airchina.crm.workorder.entity.WorkOrderFlow;
import com.airchina.crm.workorder.mapper.WorkOrderFlowMapper;
import com.airchina.crm.workorder.entity.WorkOrder;
import com.airchina.crm.workorder.mapper.WorkOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 工单流程引擎（状态机）
 *
 * 状态流转规则：
 *   CREATED ──► ASSIGNED ──► IN_PROGRESS ──► COMPLETED ──► CLOSED
 *      │            │              │
 *      └────────────┴──────────────┴──────────────────────► CLOSED
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkOrderFlowEngine {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderFlowMapper workOrderFlowMapper;

    /**
     * 执行状态流转
     *
     * @param orderId    工单ID
     * @param targetCode 目标状态
     * @param operator   操作人
     * @param remark     备注
     */
    @Transactional(rollbackFor = Exception.class)
    public void transit(Long orderId, String targetCode, String operator, String remark) {
        // 1. 查询工单
        WorkOrder order = workOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("工单不存在");
        }

        // 2. 解析当前状态和目标状态
        WorkOrderStatus currentStatus = WorkOrderStatus.fromCode(order.getStatus());
        WorkOrderStatus targetStatus = WorkOrderStatus.fromCode(targetCode);

        // 3. 状态机校验：是否允许流转
        if (!currentStatus.canTransitTo(targetStatus)) {
            throw new BizException("工单状态不允许从 " + currentStatus.getDesc() + " 变更为 " + targetStatus.getDesc());
        }

        // 4. 更新工单状态
        String fromCode = currentStatus.getCode();
        order.setStatus(targetCode);

        // 设置相关时间字段
        switch (targetStatus) {
            case ASSIGNED:
                order.setAssignedAt(LocalDateTime.now());
                break;
            case COMPLETED:
                order.setCompletedAt(LocalDateTime.now());
                break;
            default:
                break;
        }

        workOrderMapper.updateById(order);

        // 5. 写流转记录
        WorkOrderFlow flow = new WorkOrderFlow();
        flow.setOrderId(orderId);
        flow.setFromStatus(fromCode);
        flow.setToStatus(targetCode);
        flow.setOperator(operator);
        flow.setRemark(remark);
        workOrderFlowMapper.insert(flow);

        log.info("工单状态流转：orderId={}, {} -> {}, operator={}", orderId, fromCode, targetCode, operator);
    }
}
