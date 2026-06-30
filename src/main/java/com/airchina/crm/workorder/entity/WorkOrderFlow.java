package com.airchina.crm.workorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单流转记录实体
 */
@Data
@TableName("t_work_order_flow")
public class WorkOrderFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String fromStatus;

    private String toStatus;

    private String operator;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
