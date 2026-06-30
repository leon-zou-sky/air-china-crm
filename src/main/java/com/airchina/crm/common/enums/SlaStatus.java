package com.airchina.crm.common.enums;

import lombok.Getter;

/**
 * SLA 状态
 */
@Getter
public enum SlaStatus {

    NORMAL("NORMAL", "正常"),
    AT_RISK("AT_RISK", "即将超时"),
    BREACHED("BREACHED", "已超时");

    private final String code;
    private final String desc;

    SlaStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SlaStatus fromCode(String code) {
        for (SlaStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return NORMAL; // 默认正常
    }
}
