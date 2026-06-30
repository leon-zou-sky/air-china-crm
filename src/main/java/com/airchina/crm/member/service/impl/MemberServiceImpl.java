package com.airchina.crm.member.service.impl;

import com.airchina.crm.common.config.RedisCacheHelper;
import com.airchina.crm.common.enums.MemberStatus;
import com.airchina.crm.common.enums.MemberTier;
import com.airchina.crm.common.exception.BizException;
import com.airchina.crm.common.result.ResultCode;
import com.airchina.crm.member.entity.Member;
import com.airchina.crm.member.mapper.MemberMapper;
import com.airchina.crm.member.service.MemberService;
import com.airchina.crm.member.vo.MemberVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 会员服务实现
 *
 * 缓存策略（Cache Aside Pattern）：
 *   读：先查缓存 → 命中返回 / 未命中查DB → 写缓存
 *   写：更新DB → 删除缓存（不是更新缓存）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    private final RedisCacheHelper redisCacheHelper;

    private static final String CACHE_KEY_ID = "member:info:";
    private static final String CACHE_KEY_NO = "member:no:";
    private static final long CACHE_TTL_MINUTES = 30;

    @Override
    public MemberVO getMemberVOById(Long memberId) {
        // 1. 先查缓存
        String cacheKey = CACHE_KEY_ID + memberId;
        try {
            MemberVO cached = redisCacheHelper.get(cacheKey, MemberVO.class);
            if (cached != null) {
                log.debug("会员缓存命中：memberId={}", memberId);
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis 读取失败，降级查库：{}", e.getMessage());
        }

        // 2. 缓存未命中，查库
        Member member = getById(memberId);
        if (member == null) {
            throw new BizException(ResultCode.MEMBER_NOT_FOUND);
        }
        MemberVO vo = toVO(member);

        // 3. 写入缓存
        try {
            redisCacheHelper.set(cacheKey, vo, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 写入失败：{}", e.getMessage());
        }

        return vo;
    }

    @Override
    public MemberVO getMemberVOByNo(String memberNo) {
        // 1. 先查缓存
        String cacheKey = CACHE_KEY_NO + memberNo;
        try {
            MemberVO cached = redisCacheHelper.get(cacheKey, MemberVO.class);
            if (cached != null) {
                log.debug("会员缓存命中：memberNo={}", memberNo);
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis 读取失败，降级查库：{}", e.getMessage());
        }

        // 2. 缓存未命中，查库
        Member member = getMemberByNo(memberNo);
        if (member == null) {
            throw new BizException(ResultCode.MEMBER_NOT_FOUND);
        }
        MemberVO vo = toVO(member);

        // 3. 写入缓存
        try {
            redisCacheHelper.set(cacheKey, vo, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 写入失败：{}", e.getMessage());
        }

        return vo;
    }

    @Override
    public List<MemberVO> search(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            throw new BizException("关键词不能为空");
        }
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<Member>()
                .like(Member::getName, keyword)
                .or()
                .like(Member::getMobile, keyword)
                .or()
                .like(Member::getMemberNo, keyword)
                .last("LIMIT 50");

        return list(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberVO> listByTier(String tier) {
        MemberTier.fromCode(tier); // 校验等级合法性
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<Member>()
                .eq(Member::getTier, tier)
                .orderByDesc(Member::getQualifyingMiles)
                .last("LIMIT 100");

        return list(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public Member getMemberById(Long memberId) {
        Member member = getById(memberId);
        if (member == null) {
            throw new BizException(ResultCode.MEMBER_NOT_FOUND);
        }
        if (member.getStatus() != MemberStatus.NORMAL.getCode()) {
            throw new BizException(ResultCode.MEMBER_FROZEN);
        }
        return member;
    }

    @Override
    public Member getMemberByNo(String memberNo) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<Member>()
                .eq(Member::getMemberNo, memberNo);
        return getOne(wrapper);
    }

    /**
     * 清除会员缓存（会员信息变更时调用）
     */
    public void evictCache(Long memberId, String memberNo) {
        try {
            redisCacheHelper.delete(CACHE_KEY_ID + memberId);
            if (StringUtils.isNotBlank(memberNo)) {
                redisCacheHelper.delete(CACHE_KEY_NO + memberNo);
            }
            log.debug("会员缓存已清除：memberId={}", memberId);
        } catch (Exception e) {
            log.warn("缓存清除失败：{}", e.getMessage());
        }
    }

    /**
     * Entity → VO（脱敏处理）
     */
    private MemberVO toVO(Member m) {
        MemberVO vo = new MemberVO();
        vo.setMemberId(m.getMemberId());
        vo.setMemberNo(m.getMemberNo());
        vo.setName(m.getName());
        vo.setEnglishName(m.getEnglishName());
        vo.setGender(m.getGender());
        vo.setBirthday(m.getBirthday());
        vo.setMobile(desensitizeMobile(m.getMobile()));
        vo.setEmail(m.getEmail());
        vo.setIdCardNo(desensitizeIdCard(m.getIdCardNo()));
        vo.setNationality(m.getNationality());
        vo.setTier(m.getTier());
        vo.setTierDesc(MemberTier.fromCode(m.getTier()).getDesc());
        vo.setTierAchievedAt(m.getTierAchievedAt());
        vo.setTierExpiryDate(m.getTierExpiryDate());
        vo.setQualifyingMiles(m.getQualifyingMiles());
        vo.setQualifyingSegs(m.getQualifyingSegs());
        vo.setTotalMiles(m.getTotalMiles());
        vo.setRedeemableMiles(m.getRedeemableMiles());
        vo.setLifetimeMiles(m.getLifetimeMiles());
        vo.setStatus(m.getStatus());
        vo.setStatusDesc(m.getStatus() == 1 ? "正常" : m.getStatus() == 2 ? "冻结" : "注销");
        vo.setCreatedAt(m.getCreatedAt());
        return vo;
    }

    /** 手机号脱敏：138****1111 */
    private String desensitizeMobile(String mobile) {
        if (StringUtils.isBlank(mobile) || mobile.length() < 7) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }

    /** 证件号脱敏：保留前3后4 */
    private String desensitizeIdCard(String idCard) {
        if (StringUtils.isBlank(idCard) || idCard.length() < 8) {
            return "****";
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }
}
