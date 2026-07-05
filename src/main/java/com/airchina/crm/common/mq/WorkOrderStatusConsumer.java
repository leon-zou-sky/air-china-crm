package com.airchina.crm.common.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 工单状态变更消费者
 *
 * 消费场景：
 * 1. 更新CRM系统中的工单状态缓存
 * 2. 发送工单状态变更通知给相关客服
 */
@Slf4j
@Component
public class WorkOrderStatusConsumer {

    /**
     * 消费工单状态变更消息
     */
    @RabbitListener(queues = RabbitMQConfig.WO_STATUS_QUEUE)
    public void handleStatusChange(WorkOrderMessageProducer.WorkOrderStatusMessage message,
                                   Channel channel, Message msg) {
        try {
            log.info("收到工单状态变更: orderNo={}, {}→{}, operator={}",
                    message.getOrderNo(), message.getFromStatus(),
                    message.getToStatus(), message.getOperator());

            // TODO: 更新Redis缓存中的工单状态
            // redisTemplate.opsForHash().put("crm:workorder:status:" + message.getOrderId(),
            //         "status", message.getToStatus());

            // TODO: 通知相关客服（WebSocket/轮询）
            // notificationService.notifyStatusChange(message);

            // 手动确认消息
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.info("工单状态变更处理完成: orderNo={}", message.getOrderNo());

        } catch (Exception e) {
            log.error("工单状态变更处理失败: orderNo={}", message.getOrderNo(), e);
            try {
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                log.error("消息确认失败", ex);
            }
        }
    }
}
