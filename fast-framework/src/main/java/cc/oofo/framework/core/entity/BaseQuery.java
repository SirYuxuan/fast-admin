package cc.oofo.framework.core.entity;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.framework.core.query.resolver.QueryAnnotationResolver;
import lombok.Data;

@Data
public class BaseQuery<T> {

    /**
     * 分页 页码
     */
    private long page;
    /**
     * 分页 每页大小
     */
    private long pageSize;

    // ---- 数据权限字段（由 DataScopeAspect 注入，勿手动赋值）----

    /** true 表示不限制，查询全量数据；false 表示按范围过滤。 */
    private boolean dataScopeAll = true;

    /** 允许访问的部门 ID 列表（dataScopeAll=false 时生效）。 */
    private List<String> dataScopeDeptIds;

    /** 是否同时包含本人创建的数据（SELF 范围）。 */
    private boolean dataScopeIncludeSelf = false;

    /** 当前用户 ID，用于 SELF 范围过滤。 */
    private String dataScopeUserId;

    /**
     * SQL 中部门列的完整引用（含表别名），如 {@code "u.dept_id"}。
     * 用于 XML 中 {@code ${query.dataScopeDeptColumn}}，值来自 @DataScope 注解，非用户输入。
     */
    private String dataScopeDeptColumn = "dept_id";

    /**
     * SQL 中创建人列的完整引用（含表别名），如 {@code "u.created_id"}。
     * 用于 XML 中 {@code ${query.dataScopeUserColumn}}，值来自 @DataScope 注解，非用户输入。
     */
    private String dataScopeUserColumn = "created_id";

    private final QueryWrapper<T> queryWrapper = new QueryWrapper<>();

    /** 标记是否已把注解条件应用到 queryWrapper（避免重复应用） */
    private transient boolean queryWrapperBuilt = false;

    /**
     * 获取MyBatis的分页对象
     *
     * @return 分页对象
     */
    public Page<T> getMPPage() {
        return new Page<T>(page, pageSize);
    }

    /**
     * 获取MyBatis的分页对象
     *
     * @return 分页对象
     */
    public <E> Page<E> getMPPage(Class<E> clazz) {
        return new Page<E>(page, pageSize);
    }

    /**
     * 获取分页偏移量（供手写 SQL 使用）
     *
     * @return offset = (page - 1) * pageSize
     */
    public long getOffset() {
        long p = page < 1 ? 1 : page;
        return (p - 1) * pageSize;
    }

    public QueryWrapper<T> getQueryWrapper() {
        if (!queryWrapperBuilt) {
            synchronized (this) {
                if (!queryWrapperBuilt) {
                    QueryAnnotationResolver.apply(this, queryWrapper);
                    queryWrapperBuilt = true;
                }
            }
        }
        return queryWrapper;
    }
}
