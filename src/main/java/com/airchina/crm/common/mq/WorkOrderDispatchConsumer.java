package com.airchina.crm.common.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 工单派发消费者
 *
 * 消费场景：
 * 1. 首都机场地服系统接收PEK工单
 * 2. 上海机场地服系统接收SHA工单
 * 3. 白云机场地服系统接收CAN工单
 */
@Slf4j
@Component
public class WorkOrderDispatchConsumer {

    /**
     * 消费首都机场工单
     */
    @RabbitListener(queues = RabbitMQConfig.WO_PEK_QUEUE)
    public void handlePekDispatch(WorkOrderDispatchMessage message, Channel channel, Message msg) {
        try {
            log.info("收到首都机场工单派发: orderNo={}, serviceType={}, memberName={}, priority={}",
                    message.getOrderNo(), message.getServiceType(),
                    message.getMemberName(), message.getPriority());

            // TODO: 调用首都机场地服系统API
            // airportService.dispatchToPEK(message);

            // 手动确认消息
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.info("首都机场工单派发完成: orderNo={}", message.getOrderNo());

        } catch (Exception e) {
            log.error("首都机场工单派发失败: orderNo={}", message.getOrderNo(), e);
            try {
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
            } catch (IOException ex) {
                log.error("消息确认失败", ex);
            }
        }
    }

    /**
     * 消费上海机场工单
     */
    @RabbitListener(queues = RabbitMQConfig.WO_SHA_QUEUE)
    public void handleShaDispatch(WorkOrderDispatchMessage message, Channel channel, Message msg) {
        try {
            log.info("收到上海机场工单派发: orderNo={}, serviceType={}, memberName={}, priority={}",
                    message.getOrderNo(), message.getServiceType(),
                    message.getMemberName(), message.getPriority());

            // TODO: 调用上海机场地服系统API
            // airportService.dispatchToSHA(message);

            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.info("上海机场工单派发完成: orderNo={}", message.getOrderNo());

        } catch (Exception e) {
            log.error("上海机场工单派发失败: orderNo={}", message.getOrderNo(), e);
            try {
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
            } catch (IOException ex) {
                log.error("消息确认失败", ex);
            }
        }
    }

    /**
     * 消费白云机场工单
     */
    @RabbitListener(queues = RabbitMQConfig.WO_CAN_QUEUE)
    public void handleCanDispatch(WorkOrderDispatchMessage message, Channel channel, Message msg) {
        try {
            log.info("收到白云机场工单派发: orderNo={}, serviceType={}, memberName={}, priority={}",
                    message.getOrderNo(), message.getServiceType(),
                    message.getMemberName(), message.getPriority());

            // TODO: 调用白云机场地服系统API
            // airportService.dispatchToCAN(message);

            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            log.info("白云机场工单派发完成: orderNo={}", message.getOrderNo());

        } catch (Exception e) {
            log.error("白云机场工单派发失败: orderNo={}", message.getOrderNo(), e);
            try {
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
            } catch (IOException ex) {
                log.error("消息确认失败", ex);
            }
        }
    }
}
