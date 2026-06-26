package cc.oofo.utils;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * Redis 工具类
 * 
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@Component
public class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置字符串值
     * 
     * @param key   键
     * @param value 值
     */
    public void setVal(@NonNull String key, @NonNull String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取字符串值
     * 
     * @param key 键
     * @return 值
     */
    public @Nullable String getVal(@NonNull String key) {
        Object val = redisTemplate.opsForValue().get(key);
        return val == null ? null : val.toString();
    }

    /**
     * 获取字符串值，若不存在则返回默认值
     * 
     * @param key        键
     * @param defaultVal 默认值
     * @return 值
     */
    @SuppressWarnings("null")
    public @NonNull String getVal(@NonNull String key, @NonNull String defaultVal) {
        Object val = redisTemplate.opsForValue().get(key);
        return val != null ? val.toString() : defaultVal;
    }

    /**
     * 设置字符串值，并指定过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     */
    public void setVal(@NonNull String key, @NonNull String value, @NonNull Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    /**
     * 对键执行原子自增（步长 1）
     *
     * @param key 键
     * @return 自增后的值
     */
    public @Nullable Long increment(@NonNull String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 对键执行原子自增
     *
     * @param key   键
     * @param delta 步长
     * @return 自增后的值
     */
    public @Nullable Long increment(@NonNull String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 为已存在的键设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @return 设置成功返回 true，键不存在返回 false
     */
    public @Nullable Boolean expire(@NonNull String key, @NonNull Duration timeout) {
        return redisTemplate.expire(key, timeout);
    }

    /**
     * 获取键的剩余过期时间（秒）
     *
     * @param key 键
     * @return 剩余秒数；-1 表示永不过期；-2 表示键不存在
     */
    public @Nullable Long getExpire(@NonNull String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 存在返回 true
     */
    public @Nullable Boolean hasKey(@NonNull String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 删除键
     *
     * @param key 键
     * @return 删除成功返回 true
     */
    public @Nullable Boolean delete(@NonNull String key) {
        return redisTemplate.delete(key);
    }

}
