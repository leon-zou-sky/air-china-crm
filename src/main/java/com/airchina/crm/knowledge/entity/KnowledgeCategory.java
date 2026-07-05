package com.airchina.crm.knowledge.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 知识分类枚举
 */
@Getter
@AllArgsConstructor
public enum KnowledgeCategory {

    PLATFORM("PLATFORM", "平台信息", "APP下载、官网、电话销售、营业部地址"),
    TICKET("TICKET", "票规政策", "购票规则、退改签政策、行李规定"),
    FAQ("FAQ", "常见问题", "里程查询、密码重置、账户问题"),
    SERVICE("SERVICE", "服务流程", "轮椅申请、休息室使用、接送机"),
    BENEFITS("BENEFITS", "权益说明", "各等级权益、合作商户、积分兑换"),
    SCRIPT("SCRIPT", "话术模板", "开场白、结束语、投诉处理、挽留话术"),
    SYSTEM("SYSTEM", "系统操作", "CRM系统操作指南、工单处理流程");

    private final String code;
    private final String name;
    private final String description;

    public static KnowledgeCategory fromCode(String code) {
        for (KnowledgeCategory category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("未知的知识分类: " + code);
    }
}
