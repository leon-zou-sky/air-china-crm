package com.airchina.crm.ticket.service.impl;

import com.airchina.crm.common.exception.BizException;
import com.airchina.crm.common.result.ResultCode;
import com.airchina.crm.member.entity.Member;
import com.airchina.crm.member.service.MemberService;
import com.airchina.crm.miles.dto.MilesDeductDTO;
import com.airchina.crm.miles.service.MilesService;
import com.airchina.crm.ticket.entity.Ticket;
import com.airchina.crm.ticket.entity.TicketChange;
import com.airchina.crm.ticket.mapper.TicketChangeMapper;
import com.airchina.crm.ticket.mapper.TicketMapper;
import com.airchina.crm.ticket.service.TicketRuleEngine;
import com.airchina.crm.ticket.service.TicketService;
import com.airchina.crm.ticket.vo.ChangeFeeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 客票服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketMapper ticketMapper;
    private final TicketChangeMapper ticketChangeMapper;
    private final MemberService memberService;
    private final TicketRuleEngine ruleEngine;
    private final MilesService milesService;

    @Override
    public ChangeFeeVO getTicketInfo(String ticketNo) {
        Ticket ticket = getTicketByNo(ticketNo);
        ChangeFeeVO vo = new ChangeFeeVO();
        vo.setTicketNo(ticket.getTicketNo());
        vo.setTicketPrice(ticket.getTicketPrice());
        return vo;
    }

    @Override
    public ChangeFeeVO calculateChangeFee(String ticketNo, String memberNo) {
        return doCalculate(ticketNo, memberNo, "CHANGE");
    }

    @Override
    public ChangeFeeVO calculateRefundFee(String ticketNo, String memberNo) {
        return doCalculate(ticketNo, memberNo, "REFUND");
    }

    /**
     * 费用计算核心逻辑
     */
    private ChangeFeeVO doCalculate(String ticketNo, String memberNo, String changeType) {
        // 1. 查询客票
        Ticket ticket = getTicketByNo(ticketNo);
        if (!"VALID".equals(ticket.getStatus())) {
            throw new BizException(ResultCode.TICKET_STATUS_ERROR);
        }

        // 2. 查询会员等级
        Member member = memberService.getMemberByNo(memberNo);
        String memberTier = member != null ? member.getTier() : "GENERAL";

        // 3. 规则引擎计算
        TicketRuleEngine.RuleMatchResult result = ruleEngine.calculateFee(
                memberTier, ticket.getCabinClass(), changeType, ticket.getTicketPrice());

        // 4. 组装返回
        ChangeFeeVO vo = new ChangeFeeVO();
        vo.setTicketNo(ticketNo);
        vo.setChangeType(changeType);
        vo.setMemberTier(memberTier);
        vo.setCabinClass(ticket.getCabinClass());
        vo.setRuleName(result.getRuleName());
        vo.setTicketPrice(ticket.getTicketPrice());
        vo.setFeeRate(result.getFeeRate());
        vo.setFeeAmount(result.getFeeAmount());

        String action = "CHANGE".equals(changeType) ? "改签费" : "退票手续费";
        String rateStr = result.getFeeRate() != null ? result.getFeeRate().toPlainString() + "%" : "固定";
        vo.setDescription(action + "：票价 " + ticket.getTicketPrice()
                + " × 费率 " + rateStr + " = " + result.getFeeAmount() + " 元");

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeTicket(String ticketNo, String newFlightNo, String operator) {
        Ticket ticket = getTicketByNo(ticketNo);
        if (!"VALID".equals(ticket.getStatus())) {
            throw new BizException(ResultCode.TICKET_STATUS_ERROR);
        }

        // 更新客票状态
        ticket.setStatus("CHANGED");
        ticketMapper.updateById(ticket);

        // 写变更记录
        TicketChange change = new TicketChange();
        change.setTicketId(ticket.getTicketId());
        change.setChangeType("CHANGE");
        change.setOriginalFlight(ticket.getFlightNo());
        change.setNewFlight(newFlightNo);
        change.setOperator(operator);
        ticketChangeMapper.insert(change);

        log.info("客票改签成功：ticketNo={}, {} -> {}", ticketNo, ticket.getFlightNo(), newFlightNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundTicket(String ticketNo, String memberNo, String operator) {
        Ticket ticket = getTicketByNo(ticketNo);
        if (!"VALID".equals(ticket.getStatus())) {
            throw new BizException(ResultCode.TICKET_STATUS_ERROR);
        }

        // 计算退票费
        Member member = memberService.getMemberByNo(memberNo);
        String memberTier = member != null ? member.getTier() : "GENERAL";
        TicketRuleEngine.RuleMatchResult result = ruleEngine.calculateFee(
                memberTier, ticket.getCabinClass(), "REFUND", ticket.getTicketPrice());

        // 更新客票状态
        ticket.setStatus("REFUNDED");
        ticketMapper.updateById(ticket);

        // 写变更记录
        TicketChange change = new TicketChange();
        change.setTicketId(ticket.getTicketId());
        change.setChangeType("REFUND");
        change.setOriginalFlight(ticket.getFlightNo());
        change.setRefundAmount(ticket.getTicketPrice().subtract(result.getFeeAmount()));
        change.setChangeFee(result.getFeeAmount());
        change.setRuleId(result.getRuleId());
        change.setOperator(operator);
        ticketChangeMapper.insert(change);

        log.info("客票退票成功：ticketNo={}, refundAmount={}, fee={}",
                ticketNo, change.getRefundAmount(), change.getChangeFee());
    }

    private Ticket getTicketByNo(String ticketNo) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getTicketNo, ticketNo);
        Ticket ticket = ticketMapper.selectOne(wrapper);
        if (ticket == null) {
            throw new BizException(ResultCode.TICKET_NOT_FOUND);
        }
        return ticket;
    }
}
