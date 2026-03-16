package cc.oofo.system.role.entity.query;

import java.time.LocalDate;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.system.role.entity.SysRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统角色查询实体类
 * 
 * @author Sir丶雨轩
 * @since 2025/11/17
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysRoleQuery extends BaseQuery<SysRole> {

    /** 角色ID */
    private String id;

    /** 角色名称 */
    private String name;

    /** 角色状态：1启用，0禁用 */
    private Integer status;

    /** 角色备注 */
    private String remark;

    /** 开始时间 */
    private LocalDate startTime;

    /** 结束时间 */
    private LocalDate endTime;

}
