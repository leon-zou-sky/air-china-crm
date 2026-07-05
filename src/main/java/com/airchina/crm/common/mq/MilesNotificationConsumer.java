package com.airchina.crm.common.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 积分消息消费者
 *
 * 消费场景：
 * 1. 短信通知 → 调用短信平台发送积分变动通知
 * 2. 营销通知 → 更新用户画像、触发营销活动
 * 3. 操作日志 → 记录积分操作日志
 */
@Slf4j
@Component
public class MilesNotificationConsumer {

    /**
     * 消费短信通知消息
     *
     * 实际生产环境中，这里应该调用短信平台API发送短信
     */
    @RabbitListener(queues = RabbitMQConfig.MILES_SMS_QUEUE)
    public void handleSmsNotification(MilesMessage message, Channel channel, Message msg) {
        try {
            log.info("收到积分短信通知: memberId={}, memberName={}, txType={}, miles={}",
                    message.getMemberId(), message.getMemberName(),
                    message.getTxType(), message.getMiles());

            // TODO: 调用短信平台API
            // smsService.sendMilesNotification(message.getMobile(), message);

            // 模拟短信发送
            String smsContent = buildSmsContent(message);
            log.info("发送短信: mobile={}, content={}", message.getMobile(), smsContent);

            // 手动确认消息
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.info("短信通知处理完成: messageId={}", message.getMessageId());

        } catch (Exception e) {
            log.error("短信通知处理失败: messageId={}", message.getMessageId(), e);
            try {
                // 拒绝消息，不重新入队（避免无限重试）
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                log.error("消息确认失败", ex);
            }
        }
    }

    /**
     * 消费营销通知消息
     */
    @RabbitListener(queues = RabbitMQConfig.MILES_MARKETING_QUEUE)
    public void handleMarketingNotification(MilesMessage message, Channel channel, Message msg) {
        try {
            log.info("收到积分营销通知: memberId={}, txType={}, miles={}",
                    message.getMemberId(), message.getTxType(), message.getMiles());

            // TODO: 调用营销系统API
            // marketingService.updateUserProfile(message);

            // 手动确认消息
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.info("营销通知处理完成: messageId={}", message.getMessageId());

        } catch (Exception e) {
            log.error("营销通知处理失败: messageId={}", message.getMessageId(), e);
            try {
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                log.error("消息确认失败", ex);
            }
        }
    }

    /**
     * 消费操作日志消息
     */
    @RabbitListener(queues = RabbitMQConfig.MILES_LOG_QUEUE)
    public void handleLogMessage(MilesMessage message, Channel channel, Message msg) {
        try {
            log.info("收到积分操作日志: memberId={}, memberNo={}, txType={}, miles={}, operator={}",
                    message.getMemberId(), message.getMemberNo(),
                    message.getTxType(), message.getMiles(), message.getOperator());

            // TODO: 写入操作日志表
            // logService.saveMilesLog(message);

            // 手动确认消息
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.info("操作日志处理完成: messageId={}", message.getMessageId());

        } catch (Exception e) {
            log.error("操作日志处理失败: messageId={}", message.getMessageId(), e);
            try {
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                log.error("消息确认失败", ex);
            }
        }
    }

    /**
     * 构建短信内容
     */
    private String buildSmsContent(MilesMessage message) {
        switch (message.getTxType()) {
            case "EARN":
                return String.format("【凤凰知音】尊敬的%s，您的账户已到账%d积分，当前余额%d积分。",
                        message.getMemberName(), message.getMiles(), message.getBalanceAfter());
            case "REDEEM":
                return String.format("【凤凰知音】尊敬的%s，您的账户已扣减%d积分，当前余额%d积分。",
                        message.getMemberName(), Math.abs(message.getMiles()), message.getBalanceAfter());
            case "EXPIRE":
                return String.format("【凤凰知音】尊敬的%s，您有%d积分已过期，当前余额%d积分。",
                        message.getMemberName(), Math.abs(message.getMiles()), message.getBalanceAfter());
            default:
                return String.format("【凤凰知音】尊敬的%s，您的积分已变动%d，当前余额%d积分。",
                        message.getMemberName(), message.getMiles(), message.getBalanceAfter());
        }
    }
}
