package cc.oofo.system.role.entity.dto;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 系统角色 DTO
 * 
 * @author Sir丶雨轩
 * @since 2025/11/17
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysRoleDto {

    /**
     * 角色ID
     */
    private String id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色状态：1启用，0禁用
     */
    private Integer status;

    /**
     * 角色备注
     */
    private String remark;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新时间
     */
    private Timestamp updateTime;

}