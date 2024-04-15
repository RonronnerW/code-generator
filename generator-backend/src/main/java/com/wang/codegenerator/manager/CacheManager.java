package com.wang.codegenerator.manager;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CacheManager {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    /**
     * 添加缓存
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        cache.put(key, value);
        stringRedisTemplate.opsForValue().set(key, value, 24, TimeUnit.HOURS);
    }

    /**
     * 获取缓存
     * @param key
     * @return
     */
    public String get(String key) {
        String value;
        // 从本地缓存获取value
        value = cache.getIfPresent(key);
        if(StrUtil.isNotBlank(value)) {
            return value;
        }
        // 查找redis
        value = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(value)) {
            put(key, value);
            return value;
        }
        return value;
    }

    /**
     * 删除缓存
     * @param key
     */
    public void del(String key) {
        cache.invalidate(key);
        stringRedisTemplate.delete(key);
    }
    public String getCacheKey(Object parameter, String service) {
        String jsonStr = JSONUtil.toJsonStr(parameter);
        String encode = Base64Encoder.encode(jsonStr);
        return "generator:"+service+":"+encode;
    }
}
