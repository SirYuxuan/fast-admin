package cc.oofo.system.permission.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import cc.oofo.framework.core.mapper.BaseMapper;
import cc.oofo.system.permission.entity.SysUsersRoles;

/**
 * 用户和角色关联 Mapper 接口
 *
 * @author Sir丶雨轩
 * @since 2025/11/17
 */
@Mapper
public interface SysUsersRolesMapper extends BaseMapper<SysUsersRoles> {

    /**
     * 批量插入用户角色关联
     *
     * @param list 用户角色关联列表
     * @return 插入条数
     */
    int batchInsert(@Param("list") List<SysUsersRoles> list);

    /**
     * 删除指定用户的所有角色关联
     *
     * @param userId 用户ID
     */
    void deleteByUserId(@Param("userId") String userId);

}
