package cc.oofo.system.permission.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户-角色关联实体类
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_users_roles")
public class SysUsersRoles extends Model<SysUsersRoles> {

    /**
     * 用户ID（KSUID）
     */
    private String userId;

    /**
     * 角色ID（KSUID）
     */
    private String roleId;

}