package cc.oofo.system.datascope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.datascope.DataScope;
import cc.oofo.framework.datascope.DataScopeType;
import cc.oofo.system.dept.service.SysDeptService;
import cc.oofo.system.permission.entity.SysUsersRoles;
import cc.oofo.system.permission.mapper.SysUsersRolesMapper;
import cc.oofo.system.role.entity.SysRole;
import cc.oofo.system.role.mapper.SysRoleDeptMapper;
import cc.oofo.system.role.service.SysRoleService;
import cc.oofo.system.user.entity.SysUser;
import cc.oofo.system.user.mapper.SysUserMapper;
import cn.dev33.satoken.stp.StpUtil;

/**
 * 数据权限切面。
 *
 * <p>拦截所有标注了 {@link DataScope} 的 Service 方法，根据当前用户的角色配置向第一个
 * {@link BaseQuery} 参数注入数据过滤条件，XML Mapper 中的 {@code dataScopeAll}、
 * {@code dataScopeDeptIds} 等字段即可完成 SQL 过滤。
 *
 * <p>多角色合并规则：
 * <ul>
 *   <li>任意角色为 ALL → 不过滤，直接放行</li>
 *   <li>否则，取所有角色允许的部门 ID 的并集</li>
 *   <li>含 SELF 范围的角色追加"本人数据"条件</li>
 * </ul>
 *
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@Aspect
@Component
public class DataScopeAspect {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUsersRolesMapper sysUsersRolesMapper;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysRoleDeptMapper sysRoleDeptMapper;

    @Autowired
    private SysDeptService sysDeptService;

    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint pjp, DataScope dataScope) throws Throwable {
        // 未登录时跳过过滤
        if (!StpUtil.isLogin()) {
            return pjp.proceed();
        }

        String userId = StpUtil.getLoginIdAsString();

        // 找到方法参数中第一个 BaseQuery 实例
        BaseQuery<?> queryArg = findBaseQuery(pjp.getArgs());

        // 先把注解指定的列引用写入 query（无论是否需要过滤都要设置，避免 XML 中 ${} 为 null）
        if (queryArg != null) {
            String prefix = dataScope.tableAlias().isEmpty() ? "" : dataScope.tableAlias() + ".";
            queryArg.setDataScopeDeptColumn(prefix + dataScope.deptColumn());
            queryArg.setDataScopeUserColumn(prefix + dataScope.userColumn());
        }

        // 查询当前用户信息（dept_id）
        SysUser currentUser = sysUserMapper.selectById(userId);
        if (currentUser == null) {
            return pjp.proceed();
        }

        // 查询用户的角色列表
        List<String> roleIds = sysUsersRolesMapper
                .selectList(Wrappers.lambdaQuery(SysUsersRoles.class)
                        .eq(SysUsersRoles::getUserId, userId))
                .stream().map(SysUsersRoles::getRoleId).toList();

        List<SysRole> roles = roleIds.isEmpty()
                ? Collections.emptyList()
                : sysRoleService.listByIds(roleIds);

        // 任意角色含 ALL 范围 → 全量查询，不过滤
        boolean hasAllScope = roles.stream()
                .anyMatch(r -> r.getDataScope() != null
                        && DataScopeType.of(r.getDataScope()) == DataScopeType.ALL);
        if (hasAllScope) {
            if (queryArg != null) queryArg.setDataScopeAll(true);
            return pjp.proceed();
        }

        // 合并所有角色的部门 ID 范围
        Set<String> deptIds = new HashSet<>();
        boolean includeSelf = false;
        String userDeptId = currentUser.getDeptId();

        for (SysRole role : roles) {
            DataScopeType type = DataScopeType.of(
                    role.getDataScope() != null ? role.getDataScope() : DataScopeType.ALL.getCode());

            switch (type) {
                case ALL -> {
                    // 已在上方处理
                }
                case DEPT_AND_BELOW -> {
                    // 本部门及所有下级部门
                    if (userDeptId != null) {
                        deptIds.addAll(sysDeptService.getDescendantIds(userDeptId));
                    }
                }
                case DEPT -> {
                    // 仅本部门
                    if (userDeptId != null) {
                        deptIds.add(userDeptId);
                    }
                }
                case CUSTOM -> {
                    // 自定义部门列表
                    List<String> customIds = sysRoleDeptMapper.selectDeptIdsByRoleId(role.getId());
                    deptIds.addAll(customIds);
                }
                case SELF -> {
                    includeSelf = true;
                }
            }
        }

        // 向 query 写入过滤条件
        if (queryArg != null) {
            queryArg.setDataScopeAll(false);
            queryArg.setDataScopeDeptIds(new ArrayList<>(deptIds));
            queryArg.setDataScopeIncludeSelf(includeSelf);
            queryArg.setDataScopeUserId(userId);
        }

        return pjp.proceed();
    }

    private BaseQuery<?> findBaseQuery(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof BaseQuery<?> q) return q;
        }
        return null;
    }
}
