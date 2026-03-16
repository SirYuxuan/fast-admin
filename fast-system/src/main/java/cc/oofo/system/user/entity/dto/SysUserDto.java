package cc.oofo.system.user.entity.dto;

import java.sql.Timestamp;
import java.util.List;

import cc.oofo.system.user.entity.enums.SysUserSex;
import cc.oofo.system.user.enums.SysUserStatus;
import lombok.Data;

/**
 * 系统用户DTO
 * 
 * @author Sir丶雨轩
 * @since 2025/11/17
 */
@Data
public class SysUserDto {

    /** 用户ID */
    private String id;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 昵称 */
    private String nickname;

    /** 性别：1男，2女，0未知 */
    private SysUserSex sex;

    /** 状态：0正常，1冻结，2锁定 */
    private SysUserStatus status;

    /** 最后登录IP */
    private String loginIp;

    /** 最后登录城市 */
    private String loginCity;

    /** 最后登录时间 */
    private Timestamp loginTime;

    /** 创建时间 */
    private Timestamp createTime;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 角色列表
     */
    private List<String> roles;

}
