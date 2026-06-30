package com.airchina.crm.member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员实体
 */
@Data
@TableName("t_member")
public class Member {

    @TableId(type = IdType.AUTO)
    private Long memberId;

    private String memberNo;

    private String name;

    private String englishName;

    private Integer gender;

    private LocalDate birthday;

    private String mobile;

    private String email;

    private String idCardNo;

    private String nationality;

    /** GENERAL/SILVER/GOLD/PLATINUM */
    private String tier;

    private LocalDateTime tierAchievedAt;

    private LocalDate tierExpiryDate;

    private Integer qualifyingMiles;

    private Integer qualifyingSegs;

    private Integer totalMiles;

    private Integer redeemableMiles;

    private Long lifetimeMiles;

    /** 1正常 2冻结 3注销 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
