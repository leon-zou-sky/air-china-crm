package com.airchina.crm.ticket.controller;

import com.airchina.crm.common.result.Result;
import com.airchina.crm.ticket.service.TicketService;
import com.airchina.crm.ticket.vo.ChangeFeeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 客票退改签接口
 */
@Tag(name = "客票退改签", description = "客票查询、改签费用计算、退票费用计算、执行改签/退票")
@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "查询客票信息", description = "根据客票号查询客票详细信息")
    @GetMapping("/{ticketNo}")
    public Result<ChangeFeeVO> getTicket(
            @Parameter(description = "客票号", required = true, example = "TK201806150001")
            @PathVariable String ticketNo) {
        return Result.ok(ticketService.getTicketInfo(ticketNo));
    }

    @Operation(summary = "计算改签费用", description = "根据客票号和会员号计算改签费用")
    @GetMapping("/change/fee")
    public Result<ChangeFeeVO> calculateChangeFee(
            @Parameter(description = "客票号", required = true, example = "TK201806150001")
            @RequestParam String ticketNo,
            @Parameter(description = "会员号", required = true, example = "CA10000001")
            @RequestParam String memberNo) {
        return Result.ok(ticketService.calculateChangeFee(ticketNo, memberNo));
    }

    @Operation(summary = "计算退票费用", description = "根据客票号和会员号计算退票手续费")
    @GetMapping("/refund/fee")
    public Result<ChangeFeeVO> calculateRefundFee(
            @Parameter(description = "客票号", required = true, example = "TK201806150001")
            @RequestParam String ticketNo,
            @Parameter(description = "会员号", required = true, example = "CA10000001")
            @RequestParam String memberNo) {
        return Result.ok(ticketService.calculateRefundFee(ticketNo, memberNo));
    }

    @Operation(summary = "执行改签", description = "将客票改签到新航班")
    @PostMapping("/change")
    public Result<Void> changeTicket(
            @Parameter(description = "客票号", required = true, example = "TK201806150001")
            @RequestParam String ticketNo,
            @Parameter(description = "新航班号", required = true, example = "CA9876")
            @RequestParam String newFlightNo,
            @Parameter(description = "操作人", example = "SYSTEM")
            @RequestParam(defaultValue = "SYSTEM") String operator) {
        ticketService.changeTicket(ticketNo, newFlightNo, operator);
        return Result.ok();
    }

    @Operation(summary = "执行退票", description = "执行客票退票，退还积分并扣减手续费")
    @PostMapping("/refund")
    public Result<Void> refundTicket(
            @Parameter(description = "客票号", required = true, example = "TK201806150001")
            @RequestParam String ticketNo,
            @Parameter(description = "会员号", required = true, example = "CA10000001")
            @RequestParam String memberNo,
            @Parameter(description = "操作人", example = "SYSTEM")
            @RequestParam(defaultValue = "SYSTEM") String operator) {
        ticketService.refundTicket(ticketNo, memberNo, operator);
        return Result.ok();
    }
}
