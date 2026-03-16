package cc.oofo.system.user.entity;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import cc.oofo.system.user.entity.enums.SysUserSex;
import cc.oofo.system.user.enums.SysUserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户实体类
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user")
public class SysUser extends BaseEntity<SysUser> {

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 状态：0正常，1冻结，2锁定
     */
    private SysUserStatus status;

    /**
     * 性别：1男，2女，0未知
     */
    private SysUserSex sex;

    /**
     * 最后登录IP（支持IPv6）
     */
    private String loginIp;

    /**
     * 最后登录城市
     */
    private String loginCity;

    /**
     * 最后登录时间
     */
    private Timestamp loginTime;

}