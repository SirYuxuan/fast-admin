package cc.oofo.system.user.entity.query;

import java.time.LocalDate;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.system.user.entity.SysUser;
import cc.oofo.system.user.entity.enums.SysUserSex;
import cc.oofo.system.user.enums.SysUserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户查询实体类
 * 
 * @author Sir丶雨轩
 * @since 2025/11/17
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysUserQuery extends BaseQuery<SysUser> {

    /** 用户ID */
    private String id;
    /**
     * 部门ID
     */
    private String deptId;

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

    /** 登录城市 */
    private String loginCity;

    /** 开始时间 */
    private LocalDate startTime;

    /** 结束时间 */
    private LocalDate endTime;

}
