package cc.oofo.system.file.storage.config;

import lombok.Data;

/**
 * 本地存储配置
 *
 * @author Sir丶雨轩
 */
@Data
public class LocalConfig implements StorageConfig {

    /** 本地根目录 */
    private String basePath;

}
