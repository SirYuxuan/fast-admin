package cc.oofo.system.file.storage.impl;

import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;

import cc.oofo.framework.exception.BizException;
import cc.oofo.system.file.storage.FileMeta;
import cc.oofo.system.file.storage.FileStorage;
import cc.oofo.system.file.storage.StorageType;
import cc.oofo.system.file.storage.UploadResult;
import cc.oofo.system.file.storage.config.OssConfig;
import cc.oofo.system.file.storage.config.StorageConfig;

/**
 * 阿里云 OSS 存储
 *
 * @author Sir丶雨轩
 */
@Component
public class OssFileStorage implements FileStorage {

    @Override
    public StorageType type() {
        return StorageType.OSS;
    }

    @Override
    public Class<? extends StorageConfig> configClass() {
        return OssConfig.class;
    }

    @Override
    public UploadResult upload(StorageConfig config, String urlPrefix, InputStream in, FileMeta meta) {
        OssConfig c = (OssConfig) config;
        String key = objectKey(c, meta.getStorageKey());
        OSS client = client(c);
        try {
            ObjectMetadata om = new ObjectMetadata();
            om.setContentLength(meta.getSize());
            if (meta.getContentType() != null) {
                om.setContentType(meta.getContentType());
            }
            client.putObject(c.getBucket(), key, in, om);
        } catch (Exception e) {
            throw new BizException("OSS 上传失败: " + e.getMessage());
        } finally {
            client.shutdown();
        }
        return UploadResult.builder()
                .storageKey(key)
                .url(joinUrl(urlPrefix, key))
                .build();
    }

    @Override
    public void delete(StorageConfig config, String storageKey) {
        OssConfig c = (OssConfig) config;
        OSS client = client(c);
        try {
            client.deleteObject(c.getBucket(), storageKey);
        } catch (Exception e) {
            throw new BizException("OSS 删除失败: " + e.getMessage());
        } finally {
            client.shutdown();
        }
    }

    @Override
    public InputStream download(StorageConfig config, String storageKey) {
        OssConfig c = (OssConfig) config;
        OSS client = client(c);
        try {
            return client.getObject(c.getBucket(), storageKey).getObjectContent();
        } catch (Exception e) {
            client.shutdown();
            throw new BizException("OSS 下载失败: " + e.getMessage());
        }
    }

    private OSS client(OssConfig c) {
        return new OSSClientBuilder().build(c.getEndpoint(), c.getAccessKey(), c.getSecretKey());
    }

    private String objectKey(OssConfig c, String storageKey) {
        if (c.getBasePath() == null || c.getBasePath().isBlank()) {
            return storageKey;
        }
        return c.getBasePath().replaceAll("/+$", "") + "/" + storageKey.replaceAll("^/+", "");
    }

}
