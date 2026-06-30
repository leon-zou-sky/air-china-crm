package com.airchina.crm.common.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 会员号生成器
 * 格式：CA + 序号(8位)
 * 示例：CA10000001
 */
public class MemberNoGenerator {

    private static final AtomicLong SEQUENCE = new AtomicLong(10000000);

    public static String generate() {
        return "CA" + SEQUENCE.incrementAndGet();
    }
}
