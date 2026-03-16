package cc.oofo.system.role.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import cc.oofo.framework.core.mapper.BaseMapper;
import cc.oofo.system.role.entity.SysRole;
import cc.oofo.system.role.entity.dto.SysRoleDto;
import cc.oofo.system.role.entity.query.SysRoleQuery;

/**
 * 系统角色 Mapper 接口
 * 
 * @author Sir丶雨轩
 * @since 2025/11/17
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询角色列表（带权限）
     * 
     * @param query 查询条件
     * @return 角色列表
     */
    List<SysRoleDto> listRoleWithPermissions(@Param("query") SysRoleQuery query);

    /**
     * 统计角色数量（带权限）
     * 
     * @param query 查询条件
     * @return 角色数量
     */
    Long countRoleWithPermissions(@Param("query") SysRoleQuery query);

}