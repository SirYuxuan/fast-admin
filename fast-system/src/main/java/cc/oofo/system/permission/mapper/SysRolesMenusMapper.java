package cc.oofo.system.permission.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import cc.oofo.framework.core.mapper.BaseMapper;
import cc.oofo.system.permission.entity.SysRolesMenus;

/**
 * 角色和菜单关联 Mapper 接口
 * 
 * @author Sir丶雨轩
 * @since 2025/11/17
 */
@Mapper
public interface SysRolesMenusMapper extends BaseMapper<SysRolesMenus> {

    /**
     * 批量插入角色菜单关联
     * 
     * @param list 角色菜单关联列表
     * @return 插入条数
     */
    int batchInsert(@Param("list") List<SysRolesMenus> list);

}
