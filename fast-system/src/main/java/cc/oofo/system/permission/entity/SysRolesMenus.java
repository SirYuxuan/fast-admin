package cc.oofo.system.permission.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-菜单关联实体类
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_roles_menus")
public class SysRolesMenus extends Model<SysRolesMenus> {

    /**
     * 角色ID（KSUID）
     */
    private String roleId;

    /**
     * 菜单ID（KSUID）
     */
    private String menuId;

}