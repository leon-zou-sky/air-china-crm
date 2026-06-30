package com.airchina.crm.ticket.service;

import com.airchina.crm.common.exception.BizException;
import com.airchina.crm.common.result.ResultCode;
import com.airchina.crm.ticket.entity.TicketChangeRule;
import com.airchina.crm.ticket.mapper.TicketChangeRuleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 退改签规则引擎
 *
 * 匹配策略（按优先级从高到低）：
 * 1. 精确匹配：会员等级 + 舱位 + 变更类型
 * 2. 模糊匹配：会员等级 + 变更类型（舱位为空=通用）
 * 3. 默认规则：变更类型（等级和舱位都为空=兜底）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketRuleEngine {

    private final TicketChangeRuleMapper ruleMapper;

    /**
     * 计算改签/退票费用
     *
     * @param memberTier  会员等级
     * @param cabinClass  舱位
     * @param changeType  变更类型（CHANGE/REFUND）
     * @param ticketPrice 票价
     * @return [费用金额, 规则名称, 费率]
     */
    public RuleMatchResult calculateFee(String memberTier, String cabinClass,
                                        String changeType, BigDecimal ticketPrice) {
        // 1. 加载所有启用的规则，按优先级降序
        LambdaQueryWrapper<TicketChangeRule> wrapper = new LambdaQueryWrapper<TicketChangeRule>()
                .eq(TicketChangeRule::getStatus, 1)
                .eq(TicketChangeRule::getChangeType, changeType)
                .orderByDesc(TicketChangeRule::getPriority);
        List<TicketChangeRule> rules = ruleMapper.selectList(wrapper);

        if (rules.isEmpty()) {
            throw new BizException(ResultCode.RULE_NOT_FOUND);
        }

        // 2. 按优先级匹配规则
        TicketChangeRule matchedRule = null;
        for (TicketChangeRule rule : rules) {
            if (isMatch(rule, memberTier, cabinClass)) {
                matchedRule = rule;
                break;
            }
        }

        if (matchedRule == null) {
            throw new BizException(ResultCode.RULE_NOT_FOUND);
        }

        // 3. 计算费用
        BigDecimal feeAmount;
        BigDecimal feeRate;
        if ("PERCENT".equals(matchedRule.getFeeType())) {
            feeRate = matchedRule.getFeeValue();
            feeAmount = ticketPrice.multiply(feeRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            // FIXED
            feeAmount = matchedRule.getFeeValue();
            feeRate = null;
        }

        RuleMatchResult result = new RuleMatchResult();
        result.setRuleName(matchedRule.getRuleName());
        result.setRuleId(matchedRule.getRuleId());
        result.setFeeAmount(feeAmount);
        result.setFeeRate(feeRate);
        return result;
    }

    /**
     * 规则匹配判断
     * rule.memberTier == null → 匹配所有等级
     * rule.cabinClass == null → 匹配所有舱位
     */
    private boolean isMatch(TicketChangeRule rule, String memberTier, String cabinClass) {
        boolean tierMatch = rule.getMemberTier() == null || rule.getMemberTier().equals(memberTier);
        boolean cabinMatch = rule.getCabinClass() == null || rule.getCabinClass().equals(cabinClass);
        return tierMatch && cabinMatch;
    }

    /**
     * 规则匹配结果
     */
    @lombok.Data
    public static class RuleMatchResult {
        private String ruleName;
        private Integer ruleId;
        private BigDecimal feeAmount;
        private BigDecimal feeRate;
    }
}
