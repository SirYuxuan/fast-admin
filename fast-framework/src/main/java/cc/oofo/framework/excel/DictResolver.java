package cc.oofo.framework.excel;

/**
 * 字典解析接口：Excel 字典转换的扩展点。
 *
 * fast-framework 不依赖 fast-system，但需要字典能力 → 通过此接口让上层注入实现。
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
public interface DictResolver {

    /** value → label，如 dictType=sys_user_status, value=1 → "正常" */
    String resolveLabel(String dictType, String value);

    /** label → value，如 dictType=sys_user_status, label="正常" → "1" */
    String resolveValue(String dictType, String label);
}
