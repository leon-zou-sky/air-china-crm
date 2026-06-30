package com.airchina.crm.member.service;

import com.airchina.crm.member.dto.MemberQueryDTO;
import com.airchina.crm.member.entity.Member;
import com.airchina.crm.member.vo.MemberVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 会员服务接口
 */
public interface MemberService extends IService<Member> {

    /**
     * 根据ID查询会员（返回VO，脱敏）
     */
    MemberVO getMemberVOById(Long memberId);

    /**
     * 根据会员号查询
     */
    MemberVO getMemberVOByNo(String memberNo);

    /**
     * 会员搜索（姓名/手机号/会员号）
     */
    List<MemberVO> search(String keyword);

    /**
     * 按等级查询
     */
    List<MemberVO> listByTier(String tier);

    /**
     * 根据ID查询实体（内部调用，不脱敏）
     */
    Member getMemberById(Long memberId);

    /**
     * 根据会员号查询实体
     */
    Member getMemberByNo(String memberNo);
}
