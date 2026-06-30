package com.airchina.crm.ticket.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 改签/退票费用计算返回
 */
@Data
public class ChangeFeeVO {

    private String ticketNo;

    /** CHANGE/REFUND */
    private String changeType;

    private String memberTier;

    private String cabinClass;

    /** 匹配的规则名称 */
    private String ruleName;

    /** 票价 */
    private BigDecimal ticketPrice;

    /** 费率 */
    private BigDecimal feeRate;

    /** 计算出的费用 */
    private BigDecimal feeAmount;

    /** 费用说明 */
    private String description;
}
