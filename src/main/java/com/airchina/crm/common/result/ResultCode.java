package com.airchina.crm.common.result;

import lombok.Getter;

/**
 * 状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    // 参数校验 4xx
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),

    // 业务错误 1xxx
    MEMBER_NOT_FOUND(1001, "会员不存在"),
    MEMBER_FROZEN(1002, "会员已冻结"),
    MILES_INSUFFICIENT(1003, "积分不足"),
    MILES_VERSION_CONFLICT(1004, "积分扣减冲突，请重试"),
    TICKET_NOT_FOUND(1005, "客票不存在"),
    TICKET_STATUS_ERROR(1006, "客票状态异常"),
    WORK_ORDER_NOT_FOUND(1007, "工单不存在"),
    WORK_ORDER_STATUS_ERROR(1008, "工单状态不允许此操作"),
    RULE_NOT_FOUND(1009, "未匹配到适用规则");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
