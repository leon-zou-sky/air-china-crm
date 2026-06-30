package com.airchina.crm.workorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单模板实体
 */
@Data
@TableName("t_work_order_template")
public class WorkOrderTemplate {

    @TableId(type = IdType.AUTO)
    private Integer templateId;

    private String serviceType;

    private String templateName;

    /** 流转节点 JSON 数组 */
    private String flowNodes;

    /** SLA时限（小时） */
    private Integer slaHours;

    private Integer defaultPriority;

    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
