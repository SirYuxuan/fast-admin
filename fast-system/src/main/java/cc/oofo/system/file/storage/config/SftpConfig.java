package cc.oofo.system.file.storage.config;

import lombok.Data;

/**
 * SFTP 配置
 *
 * @author Sir丶雨轩
 */
@Data
public class SftpConfig implements StorageConfig {

    private String host;
    private Integer port;
    private String username;
    /** 密码或私钥二选一 */
    private String password;
    /** 私钥内容（PEM） */
    private String privateKey;
    /** 私钥口令（可选） */
    private String passphrase;
    /** 上传根目录 */
    private String basePath;

}
