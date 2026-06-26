package cc.oofo.framework.datascope;

/**
 * 数据权限范围枚举。
 *
 * <p>数值与数据库 sys_role.data_scope 字段对应，数值越大权限越小：
 * <ol>
 *   <li>ALL — 全部数据（不过滤）</li>
 *   <li>DEPT_AND_BELOW — 本部门及所有下级部门</li>
 *   <li>DEPT — 仅本部门</li>
 *   <li>CUSTOM — 自定义部门范围</li>
 *   <li>SELF — 仅本人创建的数据</li>
 * </ol>
 *
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
public enum DataScopeType {

    ALL(1),
    DEPT_AND_BELOW(2),
    DEPT(3),
    CUSTOM(4),
    SELF(5);

    private final int code;

    DataScopeType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /** 根据数值查找枚举，找不到时回退为 ALL（最宽松）。 */
    public static DataScopeType of(int code) {
        for (DataScopeType t : values()) {
            if (t.code == code) return t;
        }
        return ALL;
    }
}
