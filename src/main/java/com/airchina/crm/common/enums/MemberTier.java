package com.airchina.crm.common.enums;

import lombok.Getter;

/**
 * 会员等级
 */
@Getter
public enum MemberTier {

    GENERAL("GENERAL", "普通卡"),
    SILVER("SILVER", "银卡"),
    GOLD("GOLD", "金卡"),
    PLATINUM("PLATINUM", "白金卡");

    private final String code;
    private final String desc;

    MemberTier(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MemberTier fromCode(String code) {
        for (MemberTier tier : values()) {
            if (tier.code.equals(code)) {
                return tier;
            }
        }
        throw new IllegalArgumentException("未知的会员等级: " + code);
    }
}
