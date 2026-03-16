package cc.oofo.system.menu.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import cc.oofo.framework.core.mapper.BaseMapper;
import cc.oofo.system.menu.entity.SysMenu;

/**
 * 系统菜单 Mapper 接口
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询所有有权限的菜单
     * 
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> selectAllByUserId(String userId);
}