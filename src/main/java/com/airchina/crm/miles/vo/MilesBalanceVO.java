package com.airchina.crm.miles.vo;

import lombok.Data;

/**
 * 积分余额返回
 */
@Data
public class MilesBalanceVO {

    private Long memberId;

    /** 可用积分 */
    private Integer balance;

    /** 冻结积分 */
    private Integer frozen;

    /** 累积获得 */
    private Integer totalEarned;

    /** 累积兑换 */
    private Integer totalRedeemed;

    /** 累积过期 */
    private Integer totalExpired;

    /** 乐观锁版本号 */
    private Integer version;
}
