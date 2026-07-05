package com.airchina.crm.common.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * SLA告警生产者
 *
 * 发送场景：
 * 1. SLA即将超时（AT_RISK）→ 通知钉钉/邮件告警
 * 2. SLA已超时（BREACHED）→ 通知钉钉/邮件告警 + 工单升级
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlaAlertProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送SLA告警（fanout模式，所有绑定队列都会收到）
     *
     * @param message SLA告警消息
     */
    public void sendAlert(SlaAlertMessage message) {
        String messageId = UUID.randomUUID().toString();
        message.setMessageId(messageId);
        message.setAlertTime(java.time.LocalDateTime.now());

        CorrelationData correlationData = new CorrelationData(messageId);

        // fanout 交换机，routingKey 为空
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SLA_EXCHANGE,
                "",
                message,
                correlationData
        );

        log.warn("发送SLA告警: orderNo={}, slaStatus={}, remainingMinutes={}, messageId={}",
                message.getOrderNo(), message.getSlaStatus(),
                message.getRemainingMinutes(), messageId);
    }

    /**
     * 发送SLA即将超时告警
     */
    public void sendAtRiskAlert(SlaAlertMessage message) {
        message.setSlaStatus("AT_RISK");
        message.setAlertMessage(String.format("工单 %s 即将超时，剩余 %d 分钟",
                message.getOrderNo(), message.getRemainingMinutes()));
        sendAlert(message);
    }

    /**
     * 发送SLA已超时告警
     */
    public void sendBreachedAlert(SlaAlertMessage message) {
        message.setSlaStatus("BREACHED");
        message.setAlertMessage(String.format("工单 %s 已超时 %d 分钟，请立即处理",
                message.getOrderNo(), Math.abs(message.getRemainingMinutes())));
        sendAlert(message);
    }
}
