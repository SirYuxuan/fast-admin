package cc.oofo.framework.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel 列注解
 *
 * 在字段上标注，即可被 ExcelUtil 用于导出/导入。
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Excel {

    /** 列名（表头），不填默认使用字段名 */
    String name() default "";

    /** 列顺序，越小越靠前。默认 999 在最后 */
    int sort() default 999;

    /** 列宽（默认 20，可调成 10-100） */
    int width() default 20;

    /** 导入时是否必填 */
    boolean required() default false;

    /**
     * 字典类型编码：导出时把数据库存的 value 自动转成字典 label；
     * 导入时反查 label → value。
     * 例如 dictType = "sys_user_status"
     */
    String dictType() default "";

    /** 日期格式，默认 yyyy-MM-dd HH:mm:ss */
    String dateFormat() default "yyyy-MM-dd HH:mm:ss";

    /**
     * 0/1 → 是/否 类型映射。
     * 用法 yesNo = {"否", "是"}（索引 0 是 false/0，索引 1 是 true/1）
     */
    String[] yesNo() default {};

    /** 用途 */
    Type type() default Type.ALL;

    /** 列宽自适应（设了 width 时此项无效） */
    boolean autoSize() default false;

    /** 模板下载时的示例数据（可选） */
    String sample() default "";

    enum Type {
        /** 导入导出都用 */
        ALL,
        /** 仅导出 */
        EXPORT_ONLY,
        /** 仅导入 */
        IMPORT_ONLY
    }
}
