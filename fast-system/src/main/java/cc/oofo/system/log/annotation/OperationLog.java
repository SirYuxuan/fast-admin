package cc.oofo.system.log.annotation;

import cc.oofo.system.log.enums.BusinessType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 *
 * 在 Controller 方法上标注后，会自动记录操作日志。
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** 操作标题/模块 */
    String title() default "";

    /** 业务类型 */
    BusinessType type() default BusinessType.OTHER;

    /** 是否保存请求参数 */
    boolean saveRequest() default true;

    /** 是否保存响应结果 */
    boolean saveResponse() default true;
}
