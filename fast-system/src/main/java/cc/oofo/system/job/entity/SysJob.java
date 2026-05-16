package cc.oofo.system.job.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时任务实体
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_job")
public class SysJob extends BaseEntity<SysJob> {

    /** 任务名称 */
    private String jobName;
    /** 任务分组 */
    private String jobGroup;
    /** Spring Bean 名 */
    private String beanName;
    /** 方法名（默认 execute） */
    private String methodName;
    /** 方法参数（JSON 字符串或简单字符串，作为单个 String 参数传入） */
    private String methodParams;
    /** Cron 表达式 */
    private String cronExpression;
    /**
     * 错过策略
     * 1=立即执行 (MISFIRE_INSTRUCTION_FIRE_ONCE_NOW)
     * 2=忽略 (MISFIRE_INSTRUCTION_DO_NOTHING)
     * 3=触发一次 (MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY)
     */
    private Integer misfirePolicy;
    /** 是否允许并发：1是 0否 */
    private Integer concurrent;
    /** 状态：1正常 0暂停 */
    private Integer status;
    /** 备注 */
    private String remark;
}
