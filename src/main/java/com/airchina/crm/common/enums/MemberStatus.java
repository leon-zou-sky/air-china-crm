package com.airchina.crm.common.enums;

import lombok.Getter;

/**
 * 会员状态
 */
@Getter
public enum MemberStatus {

    NORMAL(1, "正常"),
    FROZEN(2, "冻结"),
    CANCELLED(3, "注销");

    private final int code;
    private final String desc;

    MemberStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
