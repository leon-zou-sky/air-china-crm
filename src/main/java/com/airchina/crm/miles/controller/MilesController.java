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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 积分接口
 */
@RestController
@RequestMapping("/api/miles")
@RequiredArgsConstructor
public class MilesController {

    private final MilesService milesService;

    /**
     * 查询积分余额
     */
    @GetMapping("/balance/{memberId}")
    public Result<MilesBalanceVO> queryBalance(@PathVariable Long memberId) {
        return Result.ok(milesService.queryBalance(memberId));
    }

    /**
     * 查询积分流水（最近N条）
     */
    @GetMapping("/transactions/{memberId}")
    public Result<List<MilesTransaction>> queryRecent(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "20") int limit) {
        return Result.ok(milesService.queryRecentTransactions(memberId, limit));
    }

    /**
     * 积分流水（分页）
     */
    @GetMapping("/transactions/{memberId}/page")
    public Result<PageResult<MilesTransaction>> queryTransactionsPage(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<MilesTransaction> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MilesTransaction> wrapper = new LambdaQueryWrapper<MilesTransaction>()
                .eq(MilesTransaction::getMemberId, memberId)
                .orderByDesc(MilesTransaction::getCreatedAt);
        return Result.ok(PageResult.of(milesService.page(page, wrapper)));
    }

    /**
     * 积分扣减（乐观锁）
     */
    @PostMapping("/deduct")
    public Result<Void> deduct(@Valid @RequestBody MilesDeductDTO dto) {
        milesService.deduct(dto);
        return Result.ok();
    }

    /**
     * 积分发放
     */
    @PostMapping("/earn")
    public Result<Void> earn(@Valid @RequestBody MilesEarnDTO dto) {
        milesService.earn(dto);
        return Result.ok();
    }
}
