package cc.oofo.framework.core.query.annotation;

/**
 * 查询操作符枚举，用于标注查询条件的比较类型。
 *
 * 常见用法：
 * - EQ: 等于
 * - GT / LT / GE / LE: 大小比较
 * - LIKE / LEFT_LIKE / RIGHT_LIKE: 模糊匹配
 * - IN / NOT_IN: 集合匹配
 * - BETWEEN: 区间匹配（需要两个值）
 * - IS_NULL / IS_NOT_NULL: 空值判断
 */
public enum Operator {
    EQ,
    NE,
    GT,
    GE,
    LT,
    LE,
    LIKE,
    LEFT_LIKE,
    RIGHT_LIKE,
    IN,
    NOT_IN,
    BETWEEN,
    IS_NULL,
    IS_NOT_NULL
}
