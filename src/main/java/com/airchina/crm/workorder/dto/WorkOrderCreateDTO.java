package com.airchina.crm.workorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 创建工单入参
 */
@Schema(description = "创建工单请求参数")
@Data
public class WorkOrderCreateDTO {

    @Schema(description = "会员ID", example = "1")
    private Long memberId;

    @NotBlank(message = "服务类型不能为空")
    @Schema(description = "服务类型", required = true, example = "LOUNGE",
            allowableValues = {"LOUNGE", "WHEELCHAIR", "MEET_GREET", "HOTEL", "TRANSFER"})
    private String serviceType;

    @Schema(description = "机场代码", example = "PEK", allowableValues = {"PEK", "SHA", "CAN"})
    private String airportCode;

    @Schema(description = "航站楼", example = "T3")
    private String terminal;

    @Schema(description = "航班号", example = "CA1234")
    private String flightNo;

    @NotNull(message = "航班日期不能为空")
    @Schema(description = "航班日期", required = true, example = "2026-07-05")
    private LocalDate flightDate;

    @Schema(description = "服务时间", example = "2026-07-05T10:00:00")
    private LocalDateTime serviceTime;

    @Schema(description = "优先级（1紧急 2普通 3低）", example = "1", allowableValues = {"1", "2", "3"})
    private Integer priority;

    @Schema(description = "备注", example = "轮椅服务")
    private String remark;

    @NotBlank(message = "创建人不能为空")
    @Schema(description = "创建人（客服工号）", required = true, example = "admin")
    private String createdBy;
}
