package cc.oofo.system.file.storage.config;

import lombok.Data;

/**
 * AWS S3（兼容）配置
 *
 * @author Sir丶雨轩
 */
@Data
public class S3Config implements StorageConfig {

    /** S3 兼容服务端点；标准 AWS 留空走 region 默认 */
    private String endpoint;
    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;
    /** 可选：bucket 内的前缀目录 */
    private String basePath;
    /** 是否启用 path-style（MinIO 等兼容服务常需开启） */
    private Boolean pathStyleAccess;

}
