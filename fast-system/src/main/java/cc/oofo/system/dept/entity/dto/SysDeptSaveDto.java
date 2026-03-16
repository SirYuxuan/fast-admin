package cc.oofo.system.dept.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统部门保存DTO
 * 
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysDeptSaveDto {

    /**
     * 主键ID（编辑时传入）
     */
    private String id;

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