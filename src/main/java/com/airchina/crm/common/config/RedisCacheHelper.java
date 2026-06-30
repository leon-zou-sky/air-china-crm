package com.airchina.crm.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具（Cache Aside 模式封装）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheHelper {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Key 规范：crm:{模块}:{业务}:{标识}
     */
    private static final String KEY_PREFIX = "crm:";

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(KEY_PREFIX + key, value, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + key);
    }

    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value != null && clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        return null;
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(KEY_PREFIX + key);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(KEY_PREFIX + key);
    }

    /**
     * 设置分布式锁
     */
    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + key, value, timeout, unit);
    }
}
