package cc.oofo.system.file.storage;

import java.io.InputStream;

import cc.oofo.system.file.storage.config.StorageConfig;

/**
 * 文件存储策略
 *
 * <p>实现类通过 {@link #type()} 注册自身类型，{@link FileStorageFactory} 按激活配置
 * 选取对应实现并传入解析后的 {@link StorageConfig}。
 *
 * @author Sir丶雨轩
 */
public interface FileStorage {

    /** 该实现支持的存储类型 */
    StorageType type();

    /** 该实现使用的配置类，工厂据此把 JSON 反序列化为类型化对象 */
    Class<? extends StorageConfig> configClass();

    /** 上传 */
    UploadResult upload(StorageConfig config, String urlPrefix, InputStream in, FileMeta meta);

    /** 删除 */
    void delete(StorageConfig config, String storageKey);

    /** 下载为流（调用方负责关闭） */
    InputStream download(StorageConfig config, String storageKey);

    /** 拼访问 URL */
    default String joinUrl(String urlPrefix, String storageKey) {
        String p = urlPrefix == null ? "" : urlPrefix.replaceAll("/+$", "");
        String k = storageKey == null ? "" : storageKey.replaceAll("^/+", "");
        return p + "/" + k;
    }

}
