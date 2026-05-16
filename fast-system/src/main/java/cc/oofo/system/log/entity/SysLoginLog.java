package cc.oofo.system.log.entity;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录日志实体
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_login_log")
public class SysLoginLog extends Model<SysLoginLog> {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;
    private String username;
    private String ip;
    private String location;
    private String browser;
    private String os;
    private String device;
    /** 1成功 0失败 */
    private Integer status;
    private String msg;
    /** LOGIN/LOGOUT */
    private String type;
    private Timestamp createdAt;
}
