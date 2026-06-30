package com.airchina.crm.member.mapper;

import com.airchina.crm.member.entity.Member;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员 Mapper
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
