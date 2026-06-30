package com.airchina.crm.member.controller;

import com.airchina.crm.common.result.Result;
import com.airchina.crm.member.service.MemberService;
import com.airchina.crm.member.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员管理接口
 */
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 根据ID查询会员
     */
    @GetMapping("/{memberId}")
    public Result<MemberVO> getById(@PathVariable Long memberId) {
        return Result.ok(memberService.getMemberVOById(memberId));
    }

    /**
     * 根据会员号查询
     */
    @GetMapping("/no/{memberNo}")
    public Result<MemberVO> getByNo(@PathVariable String memberNo) {
        return Result.ok(memberService.getMemberVOByNo(memberNo));
    }

    /**
     * 会员搜索（姓名/手机号/会员号）
     */
    @GetMapping("/search")
    public Result<List<MemberVO>> search(@RequestParam String keyword) {
        return Result.ok(memberService.search(keyword));
    }

    /**
     * 按等级查询会员列表
     */
    @GetMapping("/tier/{tier}")
    public Result<List<MemberVO>> listByTier(@PathVariable String tier) {
        return Result.ok(memberService.listByTier(tier));
    }
}
