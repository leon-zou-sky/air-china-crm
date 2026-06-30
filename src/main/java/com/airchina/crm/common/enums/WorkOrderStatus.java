package com.airchina.crm.common.enums;

import lombok.Getter;

import java.util.*;

/**
 * 工单状态（状态机）
 *
 * 状态流转：
 *   CREATED ──► ASSIGNED ──► IN_PROGRESS ──► COMPLETED ──► CLOSED
 *      │            │              │
 *      └────────────┴──────────────┴──────────────────────► CLOSED
 */
@Getter
public enum WorkOrderStatus {

    CREATED("CREATED", "已创建"),
    ASSIGNED("ASSIGNED", "已指派"),
    IN_PROGRESS("IN_PROGRESS", "处理中"),
    COMPLETED("COMPLETED", "已完成"),
    CLOSED("CLOSED", "已关闭");

    private final String code;
    private final String desc;

    /** 状态流转映射（延迟初始化，避免前向引用） */
    private static final Map<WorkOrderStatus, Set<WorkOrderStatus>> TRANSITIONS = new EnumMap<>(WorkOrderStatus.class);

    static {
        TRANSITIONS.put(CREATED, EnumSet.of(ASSIGNED, CLOSED));
        TRANSITIONS.put(ASSIGNED, EnumSet.of(IN_PROGRESS, CLOSED));
        TRANSITIONS.put(IN_PROGRESS, EnumSet.of(COMPLETED, CLOSED));
        TRANSITIONS.put(COMPLETED, EnumSet.of(CLOSED));
        TRANSITIONS.put(CLOSED, EnumSet.noneOf(WorkOrderStatus.class));
    }

    WorkOrderStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 判断是否允许流转到目标状态
     */
    public boolean canTransitTo(WorkOrderStatus target) {
        return TRANSITIONS.getOrDefault(this, Collections.emptySet()).contains(target);
    }

    public static WorkOrderStatus fromCode(String code) {
        for (WorkOrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的工单状态: " + code);
    }
}
