package com.airchina.crm.workorder.service.impl;

import com.airchina.crm.common.enums.WorkOrderStatus;
import com.airchina.crm.common.exception.BizException;
import com.airchina.crm.common.result.ResultCode;
import com.airchina.crm.common.util.OrderNoGenerator;
import com.airchina.crm.workorder.dto.WorkOrderCreateDTO;
import com.airchina.crm.workorder.entity.WorkOrder;
import com.airchina.crm.workorder.entity.WorkOrderFlow;
import com.airchina.crm.workorder.entity.WorkOrderTemplate;
import com.airchina.crm.workorder.mapper.WorkOrderFlowMapper;
import com.airchina.crm.workorder.mapper.WorkOrderMapper;
import com.airchina.crm.workorder.mapper.WorkOrderTemplateMapper;
import com.airchina.crm.workorder.service.WorkOrderFlowEngine;
import com.airchina.crm.workorder.service.WorkOrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 工单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl extends ServiceImpl<WorkOrderMapper, WorkOrder> implements WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderFlowMapper workOrderFlowMapper;
    private final WorkOrderTemplateMapper templateMapper;
    private final WorkOrderFlowEngine flowEngine;

    /**
     * 创建工单
     * 流程：匹配模板 → 生成工单号 → 计算SLA → 插入 → 写流转记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder createOrder(WorkOrderCreateDTO dto) {
        // 1. 匹配模板
        LambdaQueryWrapper<WorkOrderTemplate> tw = new LambdaQueryWrapper<WorkOrderTemplate>()
                .eq(WorkOrderTemplate::getServiceType, dto.getServiceType())
                .eq(WorkOrderTemplate::getStatus, 1);
        WorkOrderTemplate template = templateMapper.selectOne(tw);
        if (template == null) {
            throw new BizException("未找到对应的服务模板: " + dto.getServiceType());
        }

        // 2. 构建工单
        WorkOrder order = new WorkOrder();
        order.setOrderNo(OrderNoGenerator.generate());
        order.setMemberId(dto.getMemberId());
        order.setServiceType(dto.getServiceType());
        order.setTemplateId(template.getTemplateId());
        order.setAirportCode(dto.getAirportCode());
        order.setTerminal(dto.getTerminal());
        order.setFlightNo(dto.getFlightNo());
        order.setFlightDate(dto.getFlightDate());
        order.setServiceTime(dto.getServiceTime());
        order.setPriority(dto.getPriority() != null ? dto.getPriority() : template.getDefaultPriority());
        order.setStatus(WorkOrderStatus.CREATED.getCode());
        order.setSlaStatus("NORMAL");
        order.setRemark(dto.getRemark());
        order.setCreatedBy(dto.getCreatedBy());

        // 3. 计算 SLA 截止时间
        LocalDateTime slaDeadline = LocalDateTime.now().plusHours(template.getSlaHours());
        order.setSlaDeadline(slaDeadline);

        // 4. 插入
        workOrderMapper.insert(order);

        // 5. 写初始流转记录
        WorkOrderFlow flow = new WorkOrderFlow();
        flow.setOrderId(order.getOrderId());
        flow.setFromStatus(null);
        flow.setToStatus(WorkOrderStatus.CREATED.getCode());
        flow.setOperator(dto.getCreatedBy());
        flow.setRemark("创建工单");
        workOrderFlowMapper.insert(flow);

        log.info("工单创建成功：orderNo={}, serviceType={}", order.getOrderNo(), order.getServiceType());
        return order;
    }

    @Override
    public WorkOrder getOrderDetail(Long orderId) {
        WorkOrder order = workOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.WORK_ORDER_NOT_FOUND);
        }
        return order;
    }

    @Override
    public void assignOrder(Long orderId, String assignedTo, String operator) {
        WorkOrder order = getOrderDetail(orderId);
        order.setAssignedTo(assignedTo);
        workOrderMapper.updateById(order);
        flowEngine.transit(orderId, WorkOrderStatus.ASSIGNED.getCode(), operator, "指派给 " + assignedTo);
    }

    @Override
    public void startProcessing(Long orderId, String operator) {
        flowEngine.transit(orderId, WorkOrderStatus.IN_PROGRESS.getCode(), operator, "开始处理");
    }

    @Override
    public void completeOrder(Long orderId, String operator) {
        flowEngine.transit(orderId, WorkOrderStatus.COMPLETED.getCode(), operator, "服务完成");
    }

    @Override
    public void closeOrder(Long orderId, String operator, String remark) {
        flowEngine.transit(orderId, WorkOrderStatus.CLOSED.getCode(), operator, remark);
    }

    @Override
    public List<WorkOrderFlow> getFlowHistory(Long orderId) {
        LambdaQueryWrapper<WorkOrderFlow> wrapper = new LambdaQueryWrapper<WorkOrderFlow>()
                .eq(WorkOrderFlow::getOrderId, orderId)
                .orderByAsc(WorkOrderFlow::getCreatedAt);
        return workOrderFlowMapper.selectList(wrapper);
    }

    @Override
    public List<WorkOrder> getActiveOrdersByMember(Long memberId) {
        List<String> activeStatuses = Arrays.asList(
                WorkOrderStatus.CREATED.getCode(),
                WorkOrderStatus.ASSIGNED.getCode(),
                WorkOrderStatus.IN_PROGRESS.getCode()
        );
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<WorkOrder>()
                .eq(WorkOrder::getMemberId, memberId)
                .in(WorkOrder::getStatus, activeStatuses)
                .orderByDesc(WorkOrder::getCreatedAt);
        return list(wrapper);
    }
}
