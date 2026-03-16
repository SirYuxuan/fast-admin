package cc.oofo.utils;

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

}
