package com.airchina.crm.miles.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分账户实体
 */
@Data
@TableName("t_miles_account")
public class MilesAccount {

    /** 会员ID（主键） */
    @TableId(type = IdType.INPUT)
    private Long memberId;

    /** 当前可用积分 */
    private Integer balance;

    /** 冻结积分 */
    private Integer frozen;

    /** 累积获得积分 */
    private Integer totalEarned;

    /** 累积兑换积分 */
    private Integer totalRedeemed;

    /** 累积过期积分 */
    private Integer totalExpired;

    /** 乐观锁版本号 */
    private Integer version;

    private LocalDateTime updatedAt;
}
