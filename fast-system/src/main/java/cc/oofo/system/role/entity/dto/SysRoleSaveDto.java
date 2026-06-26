package cc.oofo.system.role.entity.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 系统角色保存 DTO
 * 
 * @author Sir丶雨轩
 * @since 2025/11/17
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysRoleSaveDto {

    /**
     * 角色ID（更新时需要）
     */
    private String id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色状态：1启用，0禁用
     */
    private Integer status;

    /**
     * 角色备注
     */
    private String remark;

    /**
     * 数据范围：1-全部，2-本部门及子部门，3-本部门，4-自定义，5-仅本人
     */
    private Integer dataScope;

    /**
     * 自定义数据范围时绑定的部门ID列表（dataScope=4 时有效）
     */
    private List<String> deptIds;

    /**
     * 权限列表
     */
    private List<String> permissions;

}