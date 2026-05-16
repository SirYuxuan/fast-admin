package cc.oofo.system.log.entity;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志实体
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_operation_log")
public class SysOperationLog extends Model<SysOperationLog> {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 操作模块/标题 */
    private String title;
    /** 业务类型 */
    private String businessType;
    /** 方法名 ClassName.methodName */
    private String method;
    /** HTTP 请求方式 */
    private String requestMethod;
    /** 操作类别 */
    private String operatorType;
    /** 用户ID */
    private String userId;
    /** 用户名 */
    private String username;
    /** 请求URL */
    private String url;
    /** 操作 IP */
    private String ip;
    /** 操作地点 */
    private String location;
    /** 请求参数 JSON */
    private String requestParams;
    /** 响应结果 JSON */
    private String responseResult;
    /** 状态：1成功 0失败 */
    private Integer status;
    /** 错误消息 */
    private String errorMsg;
    /** 耗时(ms) */
    private Long costTime;
    /** 创建时间 */
    private Timestamp createdAt;
}
