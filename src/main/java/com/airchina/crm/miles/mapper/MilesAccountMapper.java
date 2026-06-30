package com.airchina.crm.miles.mapper;

import com.airchina.crm.miles.entity.MilesAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 积分账户 Mapper
 */
@Mapper
public interface MilesAccountMapper extends BaseMapper<MilesAccount> {

    /**
     * 乐观锁扣减积分
     * @return 影响行数：1=成功，0=失败（余额不足或版本冲突）
     */
    @Update("UPDATE t_miles_account " +
            "SET balance = balance - #{miles}, " +
            "    total_redeemed = total_redeemed + #{miles}, " +
            "    version = version + 1, " +
            "    updated_at = NOW() " +
            "WHERE member_id = #{memberId} " +
            "  AND balance >= #{miles} " +
            "  AND version = #{version}")
    int deductMiles(@Param("memberId") Long memberId,
                    @Param("miles") int miles,
                    @Param("version") int version);

    /**
     * 积分累积
     */
    @Update("UPDATE t_miles_account " +
            "SET balance = balance + #{miles}, " +
            "    total_earned = total_earned + #{miles}, " +
            "    version = version + 1, " +
            "    updated_at = NOW() " +
            "WHERE member_id = #{memberId}")
    int earnMiles(@Param("memberId") Long memberId, @Param("miles") int miles);
}
