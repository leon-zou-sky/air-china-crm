package com.airchina.crm.miles.controller;

import com.airchina.crm.common.result.PageResult;
import com.airchina.crm.common.result.Result;
import com.airchina.crm.miles.dto.MilesDeductDTO;
import com.airchina.crm.miles.dto.MilesEarnDTO;
import com.airchina.crm.miles.entity.MilesTransaction;
import com.airchina.crm.miles.service.MilesService;
import com.airchina.crm.miles.vo.MilesBalanceVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 积分接口
 */
@Tag(name = "积分管理", description = "积分余额查询、流水查询、积分扣减、积分发放")
@RestController
@RequestMapping("/api/miles")
@RequiredArgsConstructor
public class MilesController {

    private final MilesService milesService;

    @Operation(summary = "查询积分余额", description = "查询指定会员的积分账户余额信息")
    @GetMapping("/balance/{memberId}")
    public Result<MilesBalanceVO> queryBalance(
            @Parameter(description = "会员ID", required = true, example = "1")
            @PathVariable Long memberId) {
        return Result.ok(milesService.queryBalance(memberId));
    }

    @Operation(summary = "查询积分流水", description = "查询指定会员最近N条积分变动记录")
    @GetMapping("/transactions/{memberId}")
    public Result<List<MilesTransaction>> queryRecent(
            @Parameter(description = "会员ID", required = true, example = "1")
            @PathVariable Long memberId,
            @Parameter(description = "查询条数", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        return Result.ok(milesService.queryRecentTransactions(memberId, limit));
    }

    @Operation(summary = "积分流水分页", description = "分页查询指定会员的积分变动记录")
    @GetMapping("/transactions/{memberId}/page")
    public Result<PageResult<MilesTransaction>> queryTransactionsPage(
            @Parameter(description = "会员ID", required = true, example = "1")
            @PathVariable Long memberId,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<MilesTransaction> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MilesTransaction> wrapper = new LambdaQueryWrapper<MilesTransaction>()
                .eq(MilesTransaction::getMemberId, memberId)
                .orderByDesc(MilesTransaction::getCreatedAt);
        return Result.ok(PageResult.of(milesService.page(page, wrapper)));
    }

    @Operation(summary = "积分扣减", description = "扣减会员积分（乐观锁，自动重试3次）")
    @PostMapping("/deduct")
    public Result<Void> deduct(@Valid @RequestBody MilesDeductDTO dto) {
        milesService.deduct(dto);
        return Result.ok();
    }

    @Operation(summary = "积分发放", description = "为会员发放积分")
    @PostMapping("/earn")
    public Result<Void> earn(@Valid @RequestBody MilesEarnDTO dto) {
        milesService.earn(dto);
        return Result.ok();
    }
}
