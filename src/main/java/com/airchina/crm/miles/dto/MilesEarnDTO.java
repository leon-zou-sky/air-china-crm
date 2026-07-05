package com.airchina.crm.miles.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 积分发放入参
 */
@Schema(description = "积分发放请求参数")
@Data
public class MilesEarnDTO {

    @NotNull(message = "会员ID不能为空")
    @Schema(description = "会员ID", required = true, example = "1")
    private Long memberId;

    @NotNull(message = "积分数不能为空")
    @Min(value = 1, message = "积分数必须大于0")
    @Schema(description = "发放积分数", required = true, example = "500")
    private Integer miles;

    @Schema(description = "积分来源", example = "FLIGHT", allowableValues = {"FLIGHT", "CREDIT_CARD", "HOTEL", "SYSTEM"})
    private String source;

    @Schema(description = "关联业务ID", example = "CA1234-20260704")
    private String referenceId;

    @Schema(description = "备注", example = "航班里程累积")
    private String description;

    @Schema(description = "操作人", example = "system")
    private String operator;
}
