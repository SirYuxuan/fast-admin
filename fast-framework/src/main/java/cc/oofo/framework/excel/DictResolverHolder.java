package cc.oofo.framework.excel;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * 字典解析器持有者（可选注入）：
 * 如果 Spring 容器中没有 DictResolver 实现，字典转换会自动跳过（输出原始值）。
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Component
@RequiredArgsConstructor
public class DictResolverHolder {

    private final ObjectProvider<DictResolver> resolverProvider;

    private static DictResolver resolver;

    @PostConstruct
    public void init() {
        resolver = resolverProvider.getIfAvailable();
    }

    public static DictResolver get() {
        return resolver;
    }
}
