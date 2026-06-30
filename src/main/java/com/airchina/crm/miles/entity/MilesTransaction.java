package com.airchina.crm.miles.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 积分流水实体
 */
@Data
@TableName("t_miles_transaction")
public class MilesTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    /** EARN/REDEEM/EXPIRE/FREEZE/UNFREEZE/ADJUST */
    private String txType;

    /** 变动积分（正数增加，负数减少） */
    private Integer miles;

    /** 变动后余额 */
    private Integer balanceAfter;

    /** 来源 */
    private String source;

    /** 关联业务ID */
    private String referenceId;

    private String description;

    private LocalDate expireAt;

    private String operator;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
