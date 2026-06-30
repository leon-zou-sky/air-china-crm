package com.airchina.crm.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退改签规则实体
 */
@Data
@TableName("t_ticket_change_rule")
public class TicketChangeRule {

    @TableId(type = IdType.AUTO)
    private Integer ruleId;

    private String ruleName;

    /** 适用等级（NULL=全部） */
    private String memberTier;

    /** 适用舱位（NULL=全部） */
    private String cabinClass;

    /** CHANGE/REFUND */
    private String changeType;

    /** 距起飞小时数（NULL=不限） */
    private Integer hoursBefore;

    /** PERCENT/FIXED */
    private String feeType;

    private BigDecimal feeValue;

    private Integer priority;

    /** 1启用 0停用 */
    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
