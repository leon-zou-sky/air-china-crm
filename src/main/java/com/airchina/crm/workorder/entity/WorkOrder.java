package com.airchina.crm.workorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工单实体
 */
@Data
@TableName("t_work_order")
public class WorkOrder {

    @TableId(type = IdType.AUTO)
    private Long orderId;

    private String orderNo;

    private Long memberId;

    /** LOUNGE/WHEELCHAIR/MEET_GREET/HOTEL/TRANSFER */
    private String serviceType;

    private Integer templateId;

    private String airportCode;

    private String terminal;

    private String flightNo;

    private LocalDate flightDate;

    private LocalDateTime serviceTime;

    /** 1紧急 2普通 3低 */
    private Integer priority;

    /** CREATED/ASSIGNED/IN_PROGRESS/COMPLETED/CLOSED */
    private String status;

    private String assignedTo;

    private LocalDateTime assignedAt;

    private LocalDateTime completedAt;

    private LocalDateTime slaDeadline;

    /** NORMAL/AT_RISK/BREACHED */
    private String slaStatus;

    private String remark;

    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
