package cc.oofo.system.dept.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 系统部门实体
 * 
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity<SysDept> {

    /**
     * 部门名称
     */
    private String name;

    /**
     * 上级部门ID
     */
    private String pid;

    /**
     * 状态：1-启用，0-禁用
     */
    private Boolean status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

}