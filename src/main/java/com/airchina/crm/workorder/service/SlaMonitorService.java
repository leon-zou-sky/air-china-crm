package com.airchina.crm.workorder.service;

import com.airchina.crm.common.enums.SlaStatus;
import com.airchina.crm.common.enums.WorkOrderStatus;
import com.airchina.crm.workorder.entity.WorkOrder;
import com.airchina.crm.workorder.mapper.WorkOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * SLA 超时监控服务
 *
 * 定时扫描活跃工单，更新 SLA 状态：
 *   剩余 > 30分钟  → NORMAL
 *   剩余 ≤ 30分钟  → AT_RISK
 *   已过截止时间   → BREACHED
 *
 * 设计为可插拔：
 *   - 当前：日志告警
 *   - 后续：可接入 MQ / 钉钉 / 邮件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlaMonitorService {

    private final WorkOrderMapper workOrderMapper;

    /** 即将超时阈值：30分钟 */
    private static final int RISK_MINUTES = 30;

    /**
     * 定时任务：每 5 分钟扫描一次活跃工单的 SLA 状态
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void scanSlaStatus() {
        log.info("====== SLA 监控扫描开始 ======");

        // 1. 查询所有活跃工单（CREATED / ASSIGNED / IN_PROGRESS）
        List<String> activeStatuses = Arrays.asList(
                WorkOrderStatus.CREATED.getCode(),
                WorkOrderStatus.ASSIGNED.getCode(),
                WorkOrderStatus.IN_PROGRESS.getCode()
        );
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<WorkOrder>()
                .in(WorkOrder::getStatus, activeStatuses)
                .isNotNull(WorkOrder::getSlaDeadline);
        List<WorkOrder> orders = workOrderMapper.selectList(wrapper);

        if (orders.isEmpty()) {
            log.info("无活跃工单，跳过扫描");
            return;
        }

        // 2. 逐个检查 SLA 状态
        int normalCount = 0;
        int riskCount = 0;
        int breachedCount = 0;

        for (WorkOrder order : orders) {
            SlaStatus newStatus = calculateSlaStatus(order.getSlaDeadline());
            SlaStatus currentStatus = SlaStatus.fromCode(order.getSlaStatus());

            // 状态有变化才更新
            if (newStatus != currentStatus) {
                updateSlaStatus(order, newStatus);
            }

            switch (newStatus) {
                case NORMAL:
                    normalCount++;
                    break;
                case AT_RISK:
                    riskCount++;
                    break;
                case BREACHED:
                    breachedCount++;
                    break;
            }
        }

        log.info("SLA 扫描完成：共 {} 个工单，正常 {}，即将超时 {}，已超时 {}",
                orders.size(), normalCount, riskCount, breachedCount);
        log.info("====== SLA 监控扫描结束 ======");
    }

    /**
     * 计算 SLA 状态
     */
    private SlaStatus calculateSlaStatus(LocalDateTime slaDeadline) {
        if (slaDeadline == null) {
            return SlaStatus.NORMAL;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(slaDeadline)) {
            return SlaStatus.BREACHED;
        }

        Duration remaining = Duration.between(now, slaDeadline);
        if (remaining.toMinutes() <= RISK_MINUTES) {
            return SlaStatus.AT_RISK;
        }

        return SlaStatus.NORMAL;
    }

    /**
     * 更新工单 SLA 状态 + 触发告警
     */
    private void updateSlaStatus(WorkOrder order, SlaStatus newStatus) {
        SlaStatus oldStatus = SlaStatus.fromCode(order.getSlaStatus());

        // 更新数据库
        LambdaUpdateWrapper<WorkOrder> updateWrapper = new LambdaUpdateWrapper<WorkOrder>()
                .eq(WorkOrder::getOrderId, order.getOrderId())
                .set(WorkOrder::getSlaStatus, newStatus.getCode());
        workOrderMapper.update(null, updateWrapper);

        // 触发告警
        alert(order, oldStatus, newStatus);
    }

    /**
     * 告警处理（可插拔设计）
     *
     * 当前实现：日志打印
     * 后续扩展：
     *   - rabbitTemplate.convertAndSend("crm.sla.exchange", "", alertMessage)
     *   - dingTalkService.sendAlert(alertMessage)
     *   - emailService.sendAlert(alertMessage)
     */
    private void alert(WorkOrder order, SlaStatus oldStatus, SlaStatus newStatus) {
        Duration remaining = Duration.between(LocalDateTime.now(), order.getSlaDeadline());
        long remainMinutes = remaining.toMinutes();

        String message = String.format("[SLA告警] 工单 %s | 服务类型: %s | 机场: %s | %s → %s | 剩余: %d分钟",
                order.getOrderNo(),
                order.getServiceType(),
                order.getAirportCode(),
                oldStatus.getDesc(),
                newStatus.getDesc(),
                remainMinutes);

        switch (newStatus) {
            case AT_RISK:
                log.warn("⚠️ {}", message);
                // TODO: 接 MQ 发消息
                // rabbitTemplate.convertAndSend("crm.sla.exchange", "", message);
                break;
            case BREACHED:
                log.error("🔴 {}", message);
                // TODO: 接 MQ 发消息
                // rabbitTemplate.convertAndSend("crm.sla.exchange", "", message);
                break;
            default:
                log.info("✅ {}", message);
                break;
        }
    }

    /**
     * 查询即将超时的工单（AT_RISK）
     */
    public List<WorkOrder> listAtRiskOrders() {
        return listBySlaStatus(SlaStatus.AT_RISK);
    }

    /**
     * 查询已超时的工单（BREACHED）
     */
    public List<WorkOrder> listBreachedOrders() {
        return listBySlaStatus(SlaStatus.BREACHED);
    }

    /**
     * 查询所有 SLA 异常工单（AT_RISK + BREACHED）
     */
    public List<WorkOrder> listSlaAbnormalOrders() {
        List<String> abnormalStatuses = Arrays.asList(
                SlaStatus.AT_RISK.getCode(),
                SlaStatus.BREACHED.getCode()
        );
        List<String> activeStatuses = Arrays.asList(
                WorkOrderStatus.CREATED.getCode(),
                WorkOrderStatus.ASSIGNED.getCode(),
                WorkOrderStatus.IN_PROGRESS.getCode()
        );
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<WorkOrder>()
                .in(WorkOrder::getSlaStatus, abnormalStatuses)
                .in(WorkOrder::getStatus, activeStatuses)
                .orderByAsc(WorkOrder::getSlaDeadline);
        return workOrderMapper.selectList(wrapper);
    }

    private List<WorkOrder> listBySlaStatus(SlaStatus slaStatus) {
        List<String> activeStatuses = Arrays.asList(
                WorkOrderStatus.CREATED.getCode(),
                WorkOrderStatus.ASSIGNED.getCode(),
                WorkOrderStatus.IN_PROGRESS.getCode()
        );
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<WorkOrder>()
                .eq(WorkOrder::getSlaStatus, slaStatus.getCode())
                .in(WorkOrder::getStatus, activeStatuses)
                .orderByAsc(WorkOrder::getSlaDeadline);
        return workOrderMapper.selectList(wrapper);
    }
}
