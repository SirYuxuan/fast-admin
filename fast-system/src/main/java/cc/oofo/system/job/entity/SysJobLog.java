package cc.oofo.system.job.entity;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时任务执行日志
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_job_log")
public class SysJobLog extends Model<SysJobLog> {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String jobId;
    private String jobName;
    private String jobGroup;
    private String beanName;
    private String methodName;
    private String methodParams;
    /** 1成功 0失败 */
    private Integer status;
    private Long costTime;
    private String errorMsg;
    private Timestamp createdAt;
}
