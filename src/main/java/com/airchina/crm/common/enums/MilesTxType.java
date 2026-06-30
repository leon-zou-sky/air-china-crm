package com.airchina.crm.common.enums;

import lombok.Getter;

/**
 * 积分变动类型
 */
@Getter
public enum MilesTxType {

    EARN("EARN", "累积"),
    REDEEM("REDEEM", "兑换扣减"),
    EXPIRE("EXPIRE", "过期"),
    FREEZE("FREEZE", "冻结"),
    UNFREEZE("UNFREEZE", "解冻"),
    ADJUST("ADJUST", "人工调整");

    private final String code;
    private final String desc;

    MilesTxType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
