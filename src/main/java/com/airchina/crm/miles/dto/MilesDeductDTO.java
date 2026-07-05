package com.airchina.crm.miles.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 积分扣减入参
 */
@Schema(description = "积分扣减请求参数")
@Data
public class MilesDeductDTO {

    @NotNull(message = "会员ID不能为空")
    @Schema(description = "会员ID", required = true, example = "1")
    private Long memberId;

    @NotNull(message = "扣减积分数不能为空")
    @Min(value = 1, message = "扣减积分数必须大于0")
    @Schema(description = "扣减积分数", required = true, example = "100")
    private Integer miles;

    @Schema(description = "兑换来源", example = "REDEEM", allowableValues = {"FLIGHT", "CREDIT_CARD", "HOTEL", "REDEEM", "SYSTEM"})
    private String source;

    @Schema(description = "关联业务ID（如客票号）", example = "TK201806150001")
    private String referenceId;

    @Schema(description = "备注", example = "积分兑换机票")
    private String description;

    @Schema(description = "操作人", example = "admin")
    private String operator;
}
