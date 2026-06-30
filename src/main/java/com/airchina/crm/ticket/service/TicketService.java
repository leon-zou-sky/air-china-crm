package com.airchina.crm.ticket.service;

import com.airchina.crm.ticket.vo.ChangeFeeVO;

/**
 * 客票服务接口
 */
public interface TicketService {

    /**
     * 查询客票（根据票号）
     */
    ChangeFeeVO getTicketInfo(String ticketNo);

    /**
     * 计算改签费用
     */
    ChangeFeeVO calculateChangeFee(String ticketNo, String memberNo);

    /**
     * 计算退票费用
     */
    ChangeFeeVO calculateRefundFee(String ticketNo, String memberNo);

    /**
     * 执行改签
     */
    void changeTicket(String ticketNo, String newFlightNo, String operator);

    /**
     * 执行退票
     */
    void refundTicket(String ticketNo, String memberNo, String operator);
}
