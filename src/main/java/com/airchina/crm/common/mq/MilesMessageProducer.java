package com.airchina.crm.common.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 积分消息生产者
 *
 * 发送场景：
 * 1. 积分发放（earn）→ 通知短信服务发送积分到账短信
 * 2. 积分扣减（deduct）→ 通知营销系统更新用户画像
 * 3. 积分过期（expire）→ 通知短信服务发送积分即将过期提醒
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MilesMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送积分变动通知（短信）
     */
    public void sendSmsNotification(MilesMessage message) {
        String messageId = UUID.randomUUID().toString();
        message.setMessageId(messageId);
        message.setSendTime(java.time.LocalDateTime.now());

        String routingKey = "miles.sms." + message.getTxType().toLowerCase();

        CorrelationData correlationData = new CorrelationData(messageId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MILES_EXCHANGE,
                routingKey,
                message,
                correlationData
        );

        log.info("发送积分短信通知: memberId={}, txType={}, miles={}, messageId={}",
                message.getMemberId(), message.getTxType(), message.getMiles(), messageId);
    }

    /**
     * 发送积分变动通知（营销系统）
     */
    public void sendMarketingNotification(MilesMessage message) {
        String messageId = UUID.randomUUID().toString();
        message.setMessageId(messageId);
        message.setSendTime(java.time.LocalDateTime.now());

        String routingKey = "miles.marketing." + message.getTxType().toLowerCase();

        CorrelationData correlationData = new CorrelationData(messageId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MILES_EXCHANGE,
                routingKey,
                message,
                correlationData
        );

        log.info("发送积分营销通知: memberId={}, txType={}, messageId={}",
                message.getMemberId(), message.getTxType(), messageId);
    }

    /**
     * 发送积分操作日志
     */
    public void sendLogMessage(MilesMessage message) {
        String messageId = UUID.randomUUID().toString();
        message.setMessageId(messageId);
        message.setSendTime(java.time.LocalDateTime.now());

        String routingKey = "miles.log." + message.getTxType().toLowerCase();

        CorrelationData correlationData = new CorrelationData(messageId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MILES_EXCHANGE,
                routingKey,
                message,
                correlationData
        );

        log.info("发送积分操作日志: memberId={}, txType={}, messageId={}",
                message.getMemberId(), message.getTxType(), messageId);
    }

    /**
     * 发送全部积分变动通知
     */
    public void sendAll(MilesMessage message) {
        sendSmsNotification(message);
        sendMarketingNotification(message);
        sendLogMessage(message);
    }
}
