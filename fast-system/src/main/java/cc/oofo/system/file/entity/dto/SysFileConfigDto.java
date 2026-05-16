package cc.oofo.system.file.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件存储配置展示 DTO（隐藏密钥）
 *
 * @author Sir丶雨轩
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysFileConfigDto {

    private String id;
    private String name;
    private String type;
    /** 已脱敏的配置对象 */
    private Object config;
    private String urlPrefix;
    private Boolean isActive;
    private String remark;
    private String createdAt;

}
