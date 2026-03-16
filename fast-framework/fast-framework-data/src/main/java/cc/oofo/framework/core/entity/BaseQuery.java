package cc.oofo.framework.core.entity;

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
