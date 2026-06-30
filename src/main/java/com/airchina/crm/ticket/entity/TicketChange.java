package com.airchina.crm.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客票变更记录实体
 */
@Data
@TableName("t_ticket_change")
public class TicketChange {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ticketId;

    /** CHANGE/REFUND */
    private String changeType;

    private String originalFlight;

    private String newFlight;

    private BigDecimal changeFee;

    private BigDecimal refundAmount;

    private Integer ruleId;

    private String operator;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
