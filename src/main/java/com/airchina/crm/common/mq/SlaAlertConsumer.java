package com.airchina.crm.common.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * SLA告警消费者
 *
 * 消费场景：
 * 1. 钉钉/邮件告警 → 发送告警通知给运维和客服主管
 * 2. 工单升级 → 自动升级工单优先级
 */
@Slf4j
@Component
public class SlaAlertConsumer {

    /**
     * 消费SLA告警（钉钉/邮件通知）
     */
    @RabbitListener(queues = RabbitMQConfig.SLA_ALERT_QUEUE)
    public void handleSlaAlert(SlaAlertMessage message, Channel channel, Message msg) {
        try {
            log.warn("收到SLA告警: orderNo={}, slaStatus={}, remainingMinutes={}, alertMessage={}",
                    message.getOrderNo(), message.getSlaStatus(),
                    message.getRemainingMinutes(), message.getAlertMessage());

            // TODO: 发送钉钉通知
            // dingTalkService.sendAlert(message);

            // TODO: 发送邮件通知
            // emailService.sendSlaAlert(message);

            // 手动确认消息
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.info("SLA告警处理完成: orderNo={}", message.getOrderNo());

        } catch (Exception e) {
            log.error("SLA告警处理失败: orderNo={}", message.getOrderNo(), e);
            try {
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                log.error("消息确认失败", ex);
            }
        }
    }

    /**
     * 消费SLA超时（工单升级）
     */
    @RabbitListener(queues = RabbitMQConfig.SLA_ESCALATION_QUEUE)
    public void handleSlaEscalation(SlaAlertMessage message, Channel channel, Message msg) {
        try {
            log.warn("收到SLA升级请求: orderNo={}, slaStatus={}, currentStatus={}",
                    message.getOrderNo(), message.getSlaStatus(), message.getCurrentStatus());

            // TODO: 自动升级工单优先级
            // if ("BREACHED".equals(message.getSlaStatus())) {
            //     workOrderService.escalatePriority(message.getOrderId());
            // }

            // TODO: 通知客服主管
            // supervisorService.notifyEscalation(message);

            // 手动确认消息
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.info("SLA升级处理完成: orderNo={}", message.getOrderNo());

        } catch (Exception e) {
            log.error("SLA升级处理失败: orderNo={}", message.getOrderNo(), e);
            try {
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                log.error("消息确认失败", ex);
            }
        }
    }
}
