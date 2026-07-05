package com.airchina.crm.common.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分变动消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilesMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息ID（用于幂等） */
    private String messageId;

    /** 会员ID */
    private Long memberId;

    /** 会员号 */
    private String memberNo;

    /** 会员姓名 */
    private String memberName;

    /** 手机号（脱敏后） */
    private String mobile;

    /** 积分变动类型：EARN/REDEEM/EXPIRE/ADJUST */
    private String txType;

    /** 变动积分 */
    private Integer miles;

    /** 变动后余额 */
    private Integer balanceAfter;

    /** 来源：FLIGHT/CREDIT_CARD/HOTEL/REDEEM/SYSTEM */
    private String source;

    /** 关联业务ID */
    private String referenceId;

    /** 描述 */
    private String description;

    /** 操作人 */
    private String operator;

    /** 消息发送时间 */
    private LocalDateTime sendTime;
}
