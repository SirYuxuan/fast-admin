package cc.oofo.system.user.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import cc.oofo.framework.core.mapper.BaseMapper;
import cc.oofo.system.user.entity.SysUser;
import cc.oofo.system.user.entity.dto.SysUserDto;
import cc.oofo.system.user.entity.query.SysUserQuery;

/**
 * 系统用户 Mapper 接口
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 查询用户列表（带角色）
     *
     * @param query 查询条件
     * @return 用户列表
     */
    List<SysUserDto> listUserWithRoles(@Param("query") SysUserQuery query);

    /**
     * 统计用户数量
     *
     * @param query 查询条件
     * @return 数量
     */
    Long countUserWithRoles(@Param("query") SysUserQuery query);

}