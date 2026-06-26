package cc.oofo.framework.datascope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解——标注在 Service 查询方法上，由 {@code DataScopeAspect} 拦截并向第一个
 * {@link cc.oofo.framework.core.entity.BaseQuery} 参数注入数据范围过滤条件。
 *
 * <p>使用示例：
 * <pre>{@code
 * @DataScope(tableAlias = "u", deptColumn = "dept_id", userColumn = "created_id")
 * public List<SysUserDto> listUsers(SysUserQuery query) { ... }
 * }</pre>
 *
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    /**
     * SQL 中目标表的别名（如 {@code "u"}）。
     * 若为空字符串则不加别名前缀，列引用直接使用 {@link #deptColumn()} / {@link #userColumn()}。
     */
    String tableAlias() default "";

    /**
     * 部门字段名（不含表别名）。
     */
    String deptColumn() default "dept_id";

    /**
     * 创建人字段名（不含表别名），用于"仅本人"范围。
     */
    String userColumn() default "created_id";
}
