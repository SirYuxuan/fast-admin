package cc.oofo.system.role.entity;

import java.util.Set;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统角色实体类
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_role")
public class SysRole extends BaseEntity<SysRole> {

    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色编码
     */
    private String code;
    /**
     * 角色备注
     */
    private String remark;
    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 角色对应的权限列表
     */
    @TableField(exist = false)
    private Set<String> permissions;

}