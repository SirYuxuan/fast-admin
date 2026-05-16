package cc.oofo.system.file.storage.impl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Component;

import cc.oofo.framework.exception.BizException;
import cc.oofo.system.file.storage.FileMeta;
import cc.oofo.system.file.storage.FileStorage;
import cc.oofo.system.file.storage.StorageType;
import cc.oofo.system.file.storage.UploadResult;
import cc.oofo.system.file.storage.config.LocalConfig;
import cc.oofo.system.file.storage.config.StorageConfig;

/**
 * 本地文件存储
 *
 * @author Sir丶雨轩
 */
@Component
public class LocalFileStorage implements FileStorage {

    @Override
    public StorageType type() {
        return StorageType.LOCAL;
    }

    @Override
    public Class<? extends StorageConfig> configClass() {
        return LocalConfig.class;
    }

    @Override
    public UploadResult upload(StorageConfig config, String urlPrefix, InputStream in, FileMeta meta) {
        LocalConfig c = (LocalConfig) config;
        Path target = Paths.get(c.getBasePath(), meta.getStorageKey()).normalize();
        if (!target.startsWith(Paths.get(c.getBasePath()).normalize())) {
            throw new BizException("非法路径");
        }
        try {
            Files.createDirectories(target.getParent());
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new BizException("本地文件写入失败: " + e.getMessage());
        }
        return UploadResult.builder()
                .storageKey(meta.getStorageKey())
                .url(joinUrl(urlPrefix, meta.getStorageKey()))
                .build();
    }

    @Override
    public void delete(StorageConfig config, String storageKey) {
        LocalConfig c = (LocalConfig) config;
        Path target = Paths.get(c.getBasePath(), storageKey).normalize();
        if (!target.startsWith(Paths.get(c.getBasePath()).normalize())) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (Exception e) {
            throw new BizException("本地文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream download(StorageConfig config, String storageKey) {
        LocalConfig c = (LocalConfig) config;
        Path target = Paths.get(c.getBasePath(), storageKey).normalize();
        if (!target.startsWith(Paths.get(c.getBasePath()).normalize())) {
            throw new BizException("非法路径");
        }
        try {
            return new BufferedInputStream(new FileInputStream(target.toFile()));
        } catch (Exception e) {
            throw new BizException("本地文件读取失败: " + e.getMessage());
        }
    }

}
