package cc.oofo.system.role.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cc.oofo.system.role.entity.SysRoleDept;

/**
 * 角色-部门关联 Mapper。
 *
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {

    /** 查询角色绑定的所有部门ID */
    @Select("SELECT dept_id FROM sys_role_dept WHERE role_id = #{roleId}")
    List<String> selectDeptIdsByRoleId(@Param("roleId") String roleId);

    /** 删除角色的所有部门绑定 */
    @Delete("DELETE FROM sys_role_dept WHERE role_id = #{roleId}")
    void deleteByRoleId(@Param("roleId") String roleId);
}
