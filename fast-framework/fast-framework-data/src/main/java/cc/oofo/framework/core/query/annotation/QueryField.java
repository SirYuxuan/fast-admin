package cc.oofo.framework.core.query.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标注 Query/DTO 对象中的字段，声明该字段在构建查询时应当使用的比较方式和相关信息。
 *
 * 示例：
 * 
 * <pre>
 * public class UserQuery {
 *     &#64;QueryField(operator = Operator.LIKE)
 *     private String username;
 *
 *     &#64;QueryField(operator = Operator.GT, prop = "age")
 *     private Integer minAge;
 *
 *     @QueryField(operator = Operator.BETWEEN, prop = "create_time", type = ValueType.DATE, dateFormat = "yyyy-MM-dd")
 *     private String[] createTimeRange; // [start, end]
 * }
 * </pre>
 *
 * 说明：
 * - `prop`：目标属性或数据库列名，默认为字段名。
 * - `operator`：比较操作，参考 {@link Operator}。
 * - `type`：值类型提示，参考 {@link ValueType}，可用于日期/枚举等特殊处理。
 * - `ignoreNull`：为 true 时若值为 null 或空字符串则跳过该条件。
 * - `dateFormat`：当 `type = ValueType.DATE` 时可提供格式化样式。
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryField {
    /** 比较操作，默认等于 */
    Operator operator() default Operator.EQ;

    /** 目标属性或列名，默认使用注解所在字段名 */
    String prop() default "";

    /** 值的类型提示 */
    ValueType type() default ValueType.DEFAULT;

    /** 是否在值为 null/空时跳过该条件 */
    boolean ignoreNull() default true;

    /** 当 type = DATE 时可指定的日期格式 */
    String dateFormat() default "";

    /**
     * 是否将该条件与上一个条件用 OR 连接（默认使用 AND）。
     * 注：复杂分组请在 SQL 层或更高级的构造器中处理。
     */
    boolean or() default false;
}
