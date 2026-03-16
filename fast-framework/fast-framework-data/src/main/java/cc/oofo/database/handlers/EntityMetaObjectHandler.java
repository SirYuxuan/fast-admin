package cc.oofo.database.handlers;

import java.sql.Timestamp;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

import cc.oofo.utils.IdUtil;
import cc.oofo.utils.context.AuditContextHolder;
import lombok.RequiredArgsConstructor;

/**
 * 实体元对象处理器
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Component
@RequiredArgsConstructor
public class EntityMetaObjectHandler implements MetaObjectHandler {

    private static final String DEFAULT_USER_ID = "NOT_LOGIN";
    private static final String DEFAULT_USER_NAME = "系统管理员";

    /**
     * 插入填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "id", String.class, IdUtil.generateId());
        this.strictInsertFill(metaObject, "createdAt", Timestamp.class, new Timestamp(System.currentTimeMillis()));
        this.strictInsertFill(metaObject, "createdBy", String.class, getUserName());
        this.strictInsertFill(metaObject, "createdId", String.class, getUserId());
        this.strictInsertFill(metaObject, "isDeleted", Boolean.class, false);
        this.strictInsertFill(metaObject, "updatedAt", Timestamp.class, new Timestamp(System.currentTimeMillis()));
        this.strictInsertFill(metaObject, "updatedBy", String.class, getUserName());
        this.strictInsertFill(metaObject, "updatedId", String.class, getUserId());

    }

    /**
     * 更新填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时间
        this.strictUpdateFill(metaObject, "updatedAt", Timestamp.class, new Timestamp(System.currentTimeMillis()));
        this.strictUpdateFill(metaObject, "updatedBy", String.class, getUserName());
        this.strictUpdateFill(metaObject, "updatedId", String.class, getUserId());
    }

    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID
     */
    public String getUserId() {
        return AuditContextHolder.getUserIdOrDefault(DEFAULT_USER_ID);
    }

    /**
     * 获取当前登录用户昵称
     * 
     * @return 昵称
     */
    public String getUserName() {
        return AuditContextHolder.getUserNameOrDefault(DEFAULT_USER_NAME);
    }

}
