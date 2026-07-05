package com.airchina.crm.member.controller;

import com.airchina.crm.common.result.Result;
import com.airchina.crm.member.service.MemberService;
import com.airchina.crm.member.vo.MemberVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员管理接口
 */
@Tag(name = "会员管理", description = "会员查询、搜索、等级筛选")
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "根据ID查询会员", description = "根据会员ID查询会员详细信息")
    @GetMapping("/{memberId}")
    public Result<MemberVO> getById(
            @Parameter(description = "会员ID", required = true, example = "1")
            @PathVariable Long memberId) {
        return Result.ok(memberService.getMemberVOById(memberId));
    }

    @Operation(summary = "根据会员号查询", description = "根据会员号（如CA10000001）查询会员信息")
    @GetMapping("/no/{memberNo}")
    public Result<MemberVO> getByNo(
            @Parameter(description = "会员号", required = true, example = "CA10000001")
            @PathVariable String memberNo) {
        return Result.ok(memberService.getMemberVOByNo(memberNo));
    }

    @Operation(summary = "会员搜索", description = "根据姓名、手机号或会员号模糊搜索")
    @GetMapping("/search")
    public Result<List<MemberVO>> search(
            @Parameter(description = "搜索关键词", required = true, example = "张")
            @RequestParam String keyword) {
        return Result.ok(memberService.search(keyword));
    }

    @Operation(summary = "按等级查询会员", description = "根据会员等级查询会员列表（GENERAL/SILVER/GOLD/PLATINUM）")
    @GetMapping("/tier/{tier}")
    public Result<List<MemberVO>> listByTier(
            @Parameter(description = "会员等级", required = true, example = "PLATINUM")
            @PathVariable String tier) {
        return Result.ok(memberService.listByTier(tier));
    }
}
