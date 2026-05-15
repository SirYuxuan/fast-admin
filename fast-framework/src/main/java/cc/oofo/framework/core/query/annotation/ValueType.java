package cc.oofo.framework.core.query.annotation;

/**
 * 标注字段的值类型提示，可用于在构建 SQL 或转换值时辅助处理（例如日期格式、枚举等）。
 */
public enum ValueType {
    /** 使用默认推断 */
    DEFAULT,
    STRING,
    NUMBER,
    DATE,
    BOOLEAN,
    ENUM
}
