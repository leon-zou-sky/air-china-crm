package com.airchina.crm.miles.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 积分发放入参
 */
@Data
public class MilesEarnDTO {

    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    @NotNull(message = "积分数不能为空")
    @Min(value = 1, message = "积分数必须大于0")
    private Integer miles;

    /** 来源：FLIGHT/CREDIT_CARD/HOTEL/SYSTEM */
    private String source;

    private String referenceId;

    private String description;

    private String operator;
}
