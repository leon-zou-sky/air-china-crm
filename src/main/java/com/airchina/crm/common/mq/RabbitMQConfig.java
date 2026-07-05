package com.airchina.crm.common.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 *
 * Exchange 和 Queue 设计（参考设计文档第五章）：
 *
 * 1. 积分变动通知 (crm.miles.exchange - topic)
 *    - crm.miles.sms.queue     → 短信服务
 *    - crm.miles.marketing.queue → 营销系统
 *    - crm.miles.log.queue     → 操作日志
 *
 * 2. 工单派发 (crm.workorder.exchange - direct)
 *    - crm.wo.pek.queue → 首都机场地服
 *    - crm.wo.sha.queue → 上海机场地服
 *    - crm.wo.can.queue → 白云机场地服
 *
 * 3. 工单状态变更 (crm.wo.status.exchange - direct)
 *    - crm.wo.status.queue → CRM工单服务
 *
 * 4. SLA告警 (crm.sla.exchange - fanout)
 *    - crm.sla.alert.queue      → 钉钉/邮件告警
 *    - crm.sla.escalation.queue → 工单升级
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 积分变动通知 ====================

    public static final String MILES_EXCHANGE = "crm.miles.exchange";
    public static final String MILES_SMS_QUEUE = "crm.miles.sms.queue";
    public static final String MILES_MARKETING_QUEUE = "crm.miles.marketing.queue";
    public static final String MILES_LOG_QUEUE = "crm.miles.log.queue";

    @Bean
    public TopicExchange milesExchange() {
        return ExchangeBuilder.topicExchange(MILES_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue milesSmsQueue() {
        return QueueBuilder.durable(MILES_SMS_QUEUE).build();
    }

    @Bean
    public Queue milesMarketingQueue() {
        return QueueBuilder.durable(MILES_MARKETING_QUEUE).build();
    }

    @Bean
    public Queue milesLogQueue() {
        return QueueBuilder.durable(MILES_LOG_QUEUE).build();
    }

    @Bean
    public Binding milesSmsBinding(Queue milesSmsQueue, TopicExchange milesExchange) {
        return BindingBuilder.bind(milesSmsQueue).to(milesExchange).with("miles.sms.#");
    }

    @Bean
    public Binding milesMarketingBinding(Queue milesMarketingQueue, TopicExchange milesExchange) {
        return BindingBuilder.bind(milesMarketingQueue).to(milesExchange).with("miles.marketing.#");
    }

    @Bean
    public Binding milesLogBinding(Queue milesLogQueue, TopicExchange milesExchange) {
        return BindingBuilder.bind(milesLogQueue).to(milesExchange).with("miles.log.#");
    }

    // ==================== 工单派发 ====================

    public static final String WORKORDER_EXCHANGE = "crm.workorder.exchange";
    public static final String WO_PEK_QUEUE = "crm.wo.pek.queue";
    public static final String WO_SHA_QUEUE = "crm.wo.sha.queue";
    public static final String WO_CAN_QUEUE = "crm.wo.can.queue";

    @Bean
    public DirectExchange workorderExchange() {
        return ExchangeBuilder.directExchange(WORKORDER_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue woPekQueue() {
        return QueueBuilder.durable(WO_PEK_QUEUE).build();
    }

    @Bean
    public Queue woShaQueue() {
        return QueueBuilder.durable(WO_SHA_QUEUE).build();
    }

    @Bean
    public Queue woCanQueue() {
        return QueueBuilder.durable(WO_CAN_QUEUE).build();
    }

    @Bean
    public Binding woPekBinding(Queue woPekQueue, DirectExchange workorderExchange) {
        return BindingBuilder.bind(woPekQueue).to(workorderExchange).with("PEK");
    }

    @Bean
    public Binding woShaBinding(Queue woShaQueue, DirectExchange workorderExchange) {
        return BindingBuilder.bind(woShaQueue).to(workorderExchange).with("SHA");
    }

    @Bean
    public Binding woCanBinding(Queue woCanQueue, DirectExchange workorderExchange) {
        return BindingBuilder.bind(woCanQueue).to(workorderExchange).with("CAN");
    }

    // ==================== 工单状态变更 ====================

    public static final String WO_STATUS_EXCHANGE = "crm.wo.status.exchange";
    public static final String WO_STATUS_QUEUE = "crm.wo.status.queue";

    @Bean
    public DirectExchange woStatusExchange() {
        return ExchangeBuilder.directExchange(WO_STATUS_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue woStatusQueue() {
        return QueueBuilder.durable(WO_STATUS_QUEUE).build();
    }

    @Bean
    public Binding woStatusBinding(Queue woStatusQueue, DirectExchange woStatusExchange) {
        return BindingBuilder.bind(woStatusQueue).to(woStatusExchange).with("status.change");
    }

    // ==================== SLA告警 ====================

    public static final String SLA_EXCHANGE = "crm.sla.exchange";
    public static final String SLA_ALERT_QUEUE = "crm.sla.alert.queue";
    public static final String SLA_ESCALATION_QUEUE = "crm.sla.escalation.queue";

    @Bean
    public FanoutExchange slaExchange() {
        return ExchangeBuilder.fanoutExchange(SLA_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue slaAlertQueue() {
        return QueueBuilder.durable(SLA_ALERT_QUEUE).build();
    }

    @Bean
    public Queue slaEscalationQueue() {
        return QueueBuilder.durable(SLA_ESCALATION_QUEUE).build();
    }

    @Bean
    public Binding slaAlertBinding(Queue slaAlertQueue, FanoutExchange slaExchange) {
        return BindingBuilder.bind(slaAlertQueue).to(slaExchange);
    }

    @Bean
    public Binding slaEscalationBinding(Queue slaEscalationQueue, FanoutExchange slaExchange) {
        return BindingBuilder.bind(slaEscalationQueue).to(slaExchange);
    }

    // ==================== 消息转换器 ====================

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());

        // 生产者确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // 记录日志，后续可重试
                System.err.println("消息发送失败: " + cause);
            }
        });

        // 消息退回回调
        template.setReturnsCallback(returned -> {
            System.err.println("消息被退回: " + returned.getMessage() +
                    ", 交换机: " + returned.getExchange() +
                    ", 路由键: " + returned.getRoutingKey());
        });

        return template;
    }
}
