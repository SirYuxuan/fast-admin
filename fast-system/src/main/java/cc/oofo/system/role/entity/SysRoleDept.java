package cc.oofo.system.role.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 角色-部门关联实体（自定义数据范围）。
 *
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("sys_role_dept")
public class SysRoleDept extends Model<SysRoleDept> {

    /** 角色ID */
    private String roleId;

    /** 部门ID */
    private String deptId;
}
