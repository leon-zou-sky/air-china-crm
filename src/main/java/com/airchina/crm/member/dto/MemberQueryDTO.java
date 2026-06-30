package com.airchina.crm.member.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 会员查询条件
 */
@Data
public class MemberQueryDTO {

    /** 关键词（姓名/手机号/会员号） */
    @Size(min = 2, max = 50, message = "关键词长度2-50个字符")
    private String keyword;

    /** 会员等级 */
    private String tier;

    /** 会员状态 */
    private Integer status;

    /** 当前页 */
    private Integer pageNum = 1;

    /** 每页大小 */
    private Integer pageSize = 10;
}
