package cc.oofo.system.file.storage.config;

import lombok.Data;

/**
 * 阿里云 OSS 配置
 *
 * @author Sir丶雨轩
 */
@Data
public class OssConfig implements StorageConfig {

    private String endpoint;
    private String bucket;
    private String accessKey;
    private String secretKey;
    /** 可选：bucket 内的前缀目录 */
    private String basePath;

}
