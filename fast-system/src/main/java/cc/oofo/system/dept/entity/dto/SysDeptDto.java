package cc.oofo.system.dept.entity.dto;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统部门DTO
 * 
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysDeptDto {

    /**
     * 主键ID
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
     * 上级部门名称
     */
    private String pname;

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

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 子部门列表
     */
    private List<SysDeptDto> children;

}