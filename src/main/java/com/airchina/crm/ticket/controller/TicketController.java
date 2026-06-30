package com.airchina.crm.ticket.controller;

import com.airchina.crm.common.result.Result;
import com.airchina.crm.ticket.service.TicketService;
import com.airchina.crm.ticket.vo.ChangeFeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 客票退改签接口
 */
@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * 查询客票信息
     */
    @GetMapping("/{ticketNo}")
    public Result<ChangeFeeVO> getTicket(@PathVariable String ticketNo) {
        return Result.ok(ticketService.getTicketInfo(ticketNo));
    }

    /**
     * 计算改签费用
     */
    @GetMapping("/change/fee")
    public Result<ChangeFeeVO> calculateChangeFee(@RequestParam String ticketNo,
                                                   @RequestParam String memberNo) {
        return Result.ok(ticketService.calculateChangeFee(ticketNo, memberNo));
    }

    /**
     * 计算退票费用
     */
    @GetMapping("/refund/fee")
    public Result<ChangeFeeVO> calculateRefundFee(@RequestParam String ticketNo,
                                                   @RequestParam String memberNo) {
        return Result.ok(ticketService.calculateRefundFee(ticketNo, memberNo));
    }

    /**
     * 执行改签
     */
    @PostMapping("/change")
    public Result<Void> changeTicket(@RequestParam String ticketNo,
                                     @RequestParam String newFlightNo,
                                     @RequestParam(defaultValue = "SYSTEM") String operator) {
        ticketService.changeTicket(ticketNo, newFlightNo, operator);
        return Result.ok();
    }

    /**
     * 执行退票
     */
    @PostMapping("/refund")
    public Result<Void> refundTicket(@RequestParam String ticketNo,
                                     @RequestParam String memberNo,
                                     @RequestParam(defaultValue = "SYSTEM") String operator) {
        ticketService.refundTicket(ticketNo, memberNo, operator);
        return Result.ok();
    }
}
