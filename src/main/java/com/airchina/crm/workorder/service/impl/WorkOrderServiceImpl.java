package com.airchina.crm.workorder.service.impl;

import com.airchina.crm.common.enums.WorkOrderStatus;
import com.airchina.crm.common.exception.BizException;
import com.airchina.crm.common.mq.WorkOrderDispatchMessage;
import com.airchina.crm.common.mq.WorkOrderMessageProducer;
import com.airchina.crm.common.result.ResultCode;
import com.airchina.crm.common.util.OrderNoGenerator;
import com.airchina.crm.member.entity.Member;
import com.airchina.crm.member.mapper.MemberMapper;
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
    private final WorkOrderMessageProducer workOrderMessageProducer;
    private final MemberMapper memberMapper;

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

        // 6. 发送MQ消息到机场地服系统
        sendWorkOrderDispatchMessage(order);

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
        String fromStatus = order.getStatus();
        order.setAssignedTo(assignedTo);
        workOrderMapper.updateById(order);
        flowEngine.transit(orderId, WorkOrderStatus.ASSIGNED.getCode(), operator, "指派给 " + assignedTo);

        // 发送状态变更MQ消息
        sendStatusChangeMessage(order, fromStatus, WorkOrderStatus.ASSIGNED.getCode(), operator);
    }

    @Override
    public void startProcessing(Long orderId, String operator) {
        WorkOrder order = getOrderDetail(orderId);
        String fromStatus = order.getStatus();
        flowEngine.transit(orderId, WorkOrderStatus.IN_PROGRESS.getCode(), operator, "开始处理");
        sendStatusChangeMessage(order, fromStatus, WorkOrderStatus.IN_PROGRESS.getCode(), operator);
    }

    @Override
    public void completeOrder(Long orderId, String operator) {
        WorkOrder order = getOrderDetail(orderId);
        String fromStatus = order.getStatus();
        flowEngine.transit(orderId, WorkOrderStatus.COMPLETED.getCode(), operator, "服务完成");
        sendStatusChangeMessage(order, fromStatus, WorkOrderStatus.COMPLETED.getCode(), operator);
    }

    @Override
    public void closeOrder(Long orderId, String operator, String remark) {
        WorkOrder order = getOrderDetail(orderId);
        String fromStatus = order.getStatus();
        flowEngine.transit(orderId, WorkOrderStatus.CLOSED.getCode(), operator, remark);
        sendStatusChangeMessage(order, fromStatus, WorkOrderStatus.CLOSED.getCode(), operator);
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

    /**
     * 发送工单派发MQ消息
     */
    private void sendWorkOrderDispatchMessage(WorkOrder order) {
        try {
            Member member = memberMapper.selectById(order.getMemberId());
            String memberName = member != null ? member.getName() : "未知";
            String memberNo = member != null ? member.getMemberNo() : "";
            String memberTier = member != null ? member.getTier() : "";

            WorkOrderDispatchMessage message = WorkOrderDispatchMessage.builder()
                    .orderId(order.getOrderId())
                    .orderNo(order.getOrderNo())
                    .airportCode(order.getAirportCode())
                    .terminal(order.getTerminal())
                    .serviceType(order.getServiceType())
                    .memberId(order.getMemberId())
                    .memberNo(memberNo)
                    .memberName(memberName)
                    .memberTier(memberTier)
                    .flightNo(order.getFlightNo())
                    .flightDate(order.getFlightDate())
                    .serviceTime(order.getServiceTime())
                    .priority(order.getPriority())
                    .remark(order.getRemark())
                    .build();

            workOrderMessageProducer.dispatchToAirport(message, order.getAirportCode());
            log.debug("工单派发MQ消息已发送：orderNo={}, airport={}", order.getOrderNo(), order.getAirportCode());
        } catch (Exception e) {
            log.error("工单派发MQ消息发送失败：orderNo={}", order.getOrderNo(), e);
        }
    }

    /**
     * 发送工单状态变更MQ消息
     */
    private void sendStatusChangeMessage(WorkOrder order, String fromStatus, String toStatus, String operator) {
        try {
            WorkOrderMessageProducer.WorkOrderStatusMessage message =
                    WorkOrderMessageProducer.WorkOrderStatusMessage.builder()
                            .orderId(order.getOrderId())
                            .orderNo(order.getOrderNo())
                            .fromStatus(fromStatus)
                            .toStatus(toStatus)
                            .operator(operator)
                            .changeTime(LocalDateTime.now())
                            .build();

            workOrderMessageProducer.sendStatusChange(message);
            log.debug("工单状态变更MQ消息已发送：orderNo={}, {}→{}", order.getOrderNo(), fromStatus, toStatus);
        } catch (Exception e) {
            log.error("工单状态变更MQ消息发送失败：orderNo={}", order.getOrderNo(), e);
        }
    }
}
