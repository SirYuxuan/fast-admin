package cc.oofo.flow.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import cc.oofo.flow.common.mapper.FlowIdentityMapper;
import cc.oofo.utils.context.AuditContextHolder;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;

/**
 * 当前登录用户在流程域中的身份：用户 ID、展示名、候选组。
 *
 * <p>候选组统一用前缀区分维度：{@code ROLE:<角色ID>}、{@code DEPT:<部门ID>}。
 * 部门按「本部门及子部门」语义——把用户所在部门**及其所有上级部门**都计入其组身份，
 * 这样指派给某部门时，该部门及其下级部门的成员都能命中。为兼容历史数据，角色同时保留裸 ID。</p>
 *
 * @author Sir丶雨轩
 */
@Component
@RequiredArgsConstructor
public class FlowIdentity {

    /** 候选组前缀：角色。 */
    public static final String ROLE_PREFIX = "ROLE:";
    /** 候选组前缀：部门。 */
    public static final String DEPT_PREFIX = "DEPT:";

    private final FlowIdentityMapper identityMapper;

    /** 当前用户 ID。 */
    public String userId() {
        return StpUtil.getLoginIdAsString();
    }

    /** 当前用户展示名。 */
    public String userName() {
        return AuditContextHolder.getUserNameOrDefault(userId());
    }

    /**
     * 当前用户的候选组身份：角色（裸 ID + {@code ROLE:} 前缀）+ 部门（本部门及所有上级，{@code DEPT:} 前缀）。
     */
    public List<String> groupIds() {
        String uid = userId();
        List<String> groups = new ArrayList<>();

        for (String roleId : identityMapper.selectRoleIds(uid)) {
            groups.add(roleId);                 // 兼容历史数据：裸角色 ID
            groups.add(ROLE_PREFIX + roleId);
        }

        String deptId = identityMapper.selectDeptId(uid);
        if (StringUtils.hasText(deptId)) {
            Map<String, String> pidMap = deptParentMap();
            String cur = deptId;
            int guard = 0;
            while (StringUtils.hasText(cur) && guard++ < 64) {
                groups.add(DEPT_PREFIX + cur);
                cur = pidMap.get(cur);          // 向上回溯到根部门
            }
        }
        return groups;
    }

    /** 部门 id → pid 映射。 */
    private Map<String, String> deptParentMap() {
        Map<String, String> map = new HashMap<>();
        for (Map<String, String> row : identityMapper.selectDeptParents()) {
            map.put(row.get("id"), row.get("pid"));
        }
        return map;
    }

    /** 解析任意用户 ID 的展示名。 */
    public String displayName(String userId) {
        if (userId == null || userId.isBlank()) {
            return "";
        }
        String name = identityMapper.selectDisplayName(userId);
        return name == null ? userId : name;
    }
}
