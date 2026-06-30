package com.airchina.crm.miles.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 积分扣减入参
 */
@Data
public class MilesDeductDTO {

    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    @NotNull(message = "扣减积分数不能为空")
    @Min(value = 1, message = "扣减积分数必须大于0")
    private Integer miles;

    /** 兑换类型 */
    private String source;

    /** 关联业务ID（如客票号） */
    private String referenceId;

    /** 备注 */
    private String description;

    /** 操作人 */
    private String operator;
}
