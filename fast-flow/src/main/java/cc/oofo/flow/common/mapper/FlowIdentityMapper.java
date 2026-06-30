package cc.oofo.flow.common.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 流程身份相关查询：复用系统已有的用户/角色/部门表，将角色、部门作为 Flowable 用户组。
 *
 * @author Sir丶雨轩
 */
@Mapper
public interface FlowIdentityMapper {

    /** 用户拥有的角色 ID 列表（作为流程候选组）。 */
    @Select("SELECT role_id FROM sys_users_roles WHERE user_id = #{userId}")
    List<String> selectRoleIds(@Param("userId") String userId);

    /** 用户展示名（优先昵称，回退用户名）。 */
    @Select("SELECT COALESCE(NULLIF(nickname,''), username) FROM sys_user WHERE id = #{userId}")
    String selectDisplayName(@Param("userId") String userId);

    /** 用户所属部门 ID。 */
    @Select("SELECT dept_id FROM sys_user WHERE id = #{userId}")
    String selectDeptId(@Param("userId") String userId);

    /** 全部部门的 id → pid 映射，用于向上回溯部门层级。 */
    @Select("SELECT id, pid FROM sys_dept WHERE is_deleted = 0")
    List<Map<String, String>> selectDeptParents();
}
