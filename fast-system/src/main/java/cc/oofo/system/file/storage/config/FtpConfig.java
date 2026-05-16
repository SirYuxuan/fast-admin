package cc.oofo.system.file.storage.config;

import lombok.Data;

/**
 * FTP 配置
 *
 * @author Sir丶雨轩
 */
@Data
public class FtpConfig implements StorageConfig {

    private String host;
    private Integer port;
    private String username;
    private String password;
    /** 上传根目录 */
    private String basePath;
    /** 是否使用被动模式（默认 true） */
    private Boolean passive;

}
