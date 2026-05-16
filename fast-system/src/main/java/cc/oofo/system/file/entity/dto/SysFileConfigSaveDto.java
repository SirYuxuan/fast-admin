package cc.oofo.system.file.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件存储配置保存 DTO
 *
 * @author Sir丶雨轩
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysFileConfigSaveDto {

    private String id;
    private String name;
    private String type;
    /** 类型对应的参数对象（前端按 type 给不同字段） */
    private Object config;
    private String urlPrefix;
    private String remark;

}
