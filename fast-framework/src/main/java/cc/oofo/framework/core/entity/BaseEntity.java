package cc.oofo.framework.core.entity;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 基础实体类
 *
 * @author Sir丶雨轩
 * @since 2025-11-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BaseEntity<T extends Model<?>> extends Model<T> {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 创建人名字
     */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private String createdId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Timestamp createdAt;

    /**
     * 更新人名字
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedId;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Timestamp updatedAt;

    /**
     * 逻辑删除标记：false未删除，true已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean isDeleted;
}
