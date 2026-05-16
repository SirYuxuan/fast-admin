package cc.oofo.system.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 文件存储配置实体
 *
 * @author Sir丶雨轩
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file_config")
public class SysFileConfig extends BaseEntity<SysFileConfig> {

    /** 配置名 */
    private String name;

    /** LOCAL/OSS/S3/FTP/SFTP */
    private String type;

    /** 类型相关参数，JSON 字符串 */
    private String config;

    /** 访问地址前缀 */
    private String urlPrefix;

    /** 是否激活，全表至多 1 行为 true */
    private Boolean isActive;

    /** 备注 */
    private String remark;

}
