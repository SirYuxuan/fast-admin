package cc.oofo.biz.demo.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 示例保存DTO
 *
 * @author Sir丶雨轩
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DemoSaveDto {

    /** 主键ID（编辑时传入） */
    private String id;

    /** 名称 */
    private String name;

    /** 备注 */
    private String remark;

    /** 是否启用 */
    private Boolean isEnabled;

}
