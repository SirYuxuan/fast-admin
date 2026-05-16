package cc.oofo.system.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 文件记录实体
 *
 * @author Sir丶雨轩
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SysFile extends BaseEntity<SysFile> {

    private String originalName;
    private String storageKey;
    private String url;
    private Long size;
    private String contentType;
    private String ext;
    private String hash;
    private String storageType;
    private String configId;
    private String bizType;
    private String bizId;

}
