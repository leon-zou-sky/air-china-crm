package com.airchina.crm.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客票实体
 */
@Data
@TableName("t_ticket")
public class Ticket {

    @TableId(type = IdType.AUTO)
    private Long ticketId;

    private String ticketNo;

    private Long memberId;

    private String passengerName;

    private String flightNo;

    private LocalDate flightDate;

    private String departure;

    private String arrival;

    /** F/C/Y */
    private String cabinClass;

    private BigDecimal ticketPrice;

    /** VALID/USED/CHANGED/REFUNDED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
