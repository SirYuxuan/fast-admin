package cc.oofo.system.user.entity;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import cc.oofo.framework.excel.Excel;
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
    @Excel(name = "用户名", sort = 1, required = true, width = 20, sample = "zhangsan")
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 邮箱
     */
    @Excel(name = "邮箱", sort = 3, width = 30, sample = "zhangsan@example.com")
    private String email;

    /**
     * 手机号
     */
    @Excel(name = "手机号", sort = 4, width = 18, sample = "13800138000")
    private String phone;

    /**
     * 昵称
     */
    @Excel(name = "昵称", sort = 2, width = 20, sample = "张三")
    private String nickname;

    /**
     * 头像URL
     */
    @Excel(name = "头像", sort = 99, width = 40, type = Excel.Type.EXPORT_ONLY)
    private String avatar;

    /**
     * 状态：0正常，1冻结，2锁定
     */
    @Excel(name = "状态", sort = 10, width = 12)
    private SysUserStatus status;

    /**
     * 性别：1男，2女，0未知
     */
    @Excel(name = "性别", sort = 5, width = 10)
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
    @Excel(name = "最后登录时间", sort = 80, width = 22, type = Excel.Type.EXPORT_ONLY)
    private Timestamp loginTime;

}