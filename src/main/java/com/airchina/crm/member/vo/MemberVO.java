package com.airchina.crm.member.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员返回对象（脱敏）
 */
@Data
public class MemberVO {

    private Long memberId;

    private String memberNo;

    private String name;

    private String englishName;

    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    /** 手机号脱敏：138****1111 */
    private String mobile;

    private String email;

    /** 证件号脱敏：110***********1234 */
    private String idCardNo;

    private String nationality;

    private String tier;

    private String tierDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tierAchievedAt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tierExpiryDate;

    private Integer qualifyingMiles;

    private Integer qualifyingSegs;

    private Integer totalMiles;

    private Integer redeemableMiles;

    private Long lifetimeMiles;

    private Integer status;

    private String statusDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
