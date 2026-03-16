package cc.oofo.framework.generator;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

import cc.oofo.utils.IdUtil;

/**
 * 自定义ID生成器
 */
@Component
public class CustomIdGenerator implements IdentifierGenerator {

    @Override
    public Number nextId(Object entity) {
        return null;
    }

    /**
     * 生成字符串类型的全局唯一ID
     */
    @Override
    public String nextUUID(Object entity) {
        return IdUtil.generateId();
    }
}
