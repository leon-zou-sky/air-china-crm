package com.airchina.crm.common.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 工单消息生产者
 *
 * 发送场景：
 * 1. 工单创建 → 按机场代码派发到对应机场队列
 * 2. 工单状态变更 → 通知CRM系统更新
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkOrderMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送工单派发消息（按机场路由）
     *
     * @param message 工单派发消息
     * @param airportCode 机场代码（PEK/SHA/CAN）
     */
    public void dispatchToAirport(WorkOrderDispatchMessage message, String airportCode) {
        String messageId = UUID.randomUUID().toString();
        message.setMessageId(messageId);
        message.setDispatchTime(java.time.LocalDateTime.now());

        CorrelationData correlationData = new CorrelationData(messageId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WORKORDER_EXCHANGE,
                airportCode,  // 路由键为机场代码
                message,
                correlationData
        );

        log.info("发送工单派发消息: orderNo={}, airport={}, serviceType={}, messageId={}",
                message.getOrderNo(), airportCode, message.getServiceType(), messageId);
    }

    /**
     * 发送工单状态变更消息
     */
    public void sendStatusChange(WorkOrderStatusMessage message) {
        String messageId = UUID.randomUUID().toString();
        message.setMessageId(messageId);

        CorrelationData correlationData = new CorrelationData(messageId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WO_STATUS_EXCHANGE,
                "status.change",
                message,
                correlationData
        );

        log.info("发送工单状态变更消息: orderNo={}, {}→{}, messageId={}",
                message.getOrderNo(), message.getFromStatus(), message.getToStatus(), messageId);
    }

    /**
     * 工单状态变更消息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WorkOrderStatusMessage implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String messageId;
        private Long orderId;
        private String orderNo;
        private String fromStatus;
        private String toStatus;
        private String operator;
        private String remark;
        private java.time.LocalDateTime changeTime;
    }
}
