package com.airchina.crm.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 工单号生成器
 * 格式：WO + 日期(yyyyMMdd) + 随机序号(4位)
 * 示例：WO20260630-7823
 * 使用随机数避免重启后序号冲突
 */
public class OrderNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String generate() {
        String today = LocalDate.now().format(FORMATTER);
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "WO" + today + "-" + random;
    }
}
