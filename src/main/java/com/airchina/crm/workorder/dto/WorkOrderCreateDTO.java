package com.airchina.crm.workorder.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 创建工单入参
 */
@Data
public class WorkOrderCreateDTO {

    private Long memberId;

    @NotBlank(message = "服务类型不能为空")
    private String serviceType;

    private String airportCode;

    private String terminal;

    private String flightNo;

    @NotNull(message = "航班日期不能为空")
    private LocalDate flightDate;

    private LocalDateTime serviceTime;

    /** 1紧急 2普通 3低 */
    private Integer priority;

    private String remark;

    @NotBlank(message = "创建人不能为空")
    private String createdBy;
}
