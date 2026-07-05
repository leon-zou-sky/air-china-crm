package com.airchina.crm.common.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SLA告警消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaAlertMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private String messageId;

    /** 工单ID */
    private Long orderId;

    /** 工单号 */
    private String orderNo;

    /** 机场代码 */
    private String airportCode;

    /** 服务类型 */
    private String serviceType;

    /** 会员姓名 */
    private String memberName;

    /** 会员等级 */
    private String memberTier;

    /** 当前状态 */
    private String currentStatus;

    /** SLA状态：AT_RISK/BREACHED */
    private String slaStatus;

    /** SLA截止时间 */
    private LocalDateTime slaDeadline;

    /** 剩余分钟数（负数表示已超时） */
    private Long remainingMinutes;

    /** 告警时间 */
    private LocalDateTime alertTime;

    /** 告警消息 */
    private String alertMessage;
}
