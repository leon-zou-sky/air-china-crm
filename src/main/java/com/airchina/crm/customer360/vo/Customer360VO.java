package com.airchina.crm.customer360.vo;

import com.airchina.crm.miles.vo.MilesBalanceVO;
import com.airchina.crm.workorder.entity.WorkOrder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户360视图 - 聚合返回对象
 */
@Data
public class Customer360VO {

    /** 会员信息 */
    private MemberInfo member;

    /** 积分信息 */
    private MilesBalanceVO miles;

    /** 活跃工单 */
    private List<WorkOrder> activeOrders;

    /** 数据加载时间 */
    private LocalDateTime loadedAt;

    @Data
    public static class MemberInfo {
        private Long memberId;
        private String memberNo;
        private String name;
        private String tier;
        private String tierDesc;
        private Integer qualifyingMiles;
        private Integer redeemableMiles;
        private String mobile;
    }
}
