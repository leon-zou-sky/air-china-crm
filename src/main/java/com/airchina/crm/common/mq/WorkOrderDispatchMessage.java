package com.airchina.crm.common.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工单派发消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderDispatchMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private String messageId;

    /** 工单ID */
    private Long orderId;

    /** 工单号 */
    private String orderNo;

    /** 机场代码：PEK/SHA/CAN */
    private String airportCode;

    /** 航站楼 */
    private String terminal;

    /** 服务类型：LOUNGE/WHEELCHAIR/MEET_GREET/HOTEL/TRANSFER */
    private String serviceType;

    /** 会员ID */
    private Long memberId;

    /** 会员号 */
    private String memberNo;

    /** 会员姓名 */
    private String memberName;

    /** 会员等级 */
    private String memberTier;

    /** 航班号 */
    private String flightNo;

    /** 航班日期 */
    private LocalDate flightDate;

    /** 服务时间 */
    private LocalDateTime serviceTime;

    /** 优先级：1紧急 2普通 3低 */
    private Integer priority;

    /** 备注 */
    private String remark;

    /** 派发时间 */
    private LocalDateTime dispatchTime;
}
