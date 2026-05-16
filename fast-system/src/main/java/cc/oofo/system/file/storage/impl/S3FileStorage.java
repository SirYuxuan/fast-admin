package cc.oofo.system.file.storage.impl;

import java.io.InputStream;
import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import cc.oofo.framework.exception.BizException;
import cc.oofo.system.file.storage.FileMeta;
import cc.oofo.system.file.storage.FileStorage;
import cc.oofo.system.file.storage.StorageType;
import cc.oofo.system.file.storage.UploadResult;
import cc.oofo.system.file.storage.config.S3Config;
import cc.oofo.system.file.storage.config.StorageConfig;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * S3（含兼容服务）存储
 *
 * @author Sir丶雨轩
 */
@Component
public class S3FileStorage implements FileStorage {

    @Override
    public StorageType type() {
        return StorageType.S3;
    }

    @Override
    public Class<? extends StorageConfig> configClass() {
        return S3Config.class;
    }

    @Override
    public UploadResult upload(StorageConfig config, String urlPrefix, InputStream in, FileMeta meta) {
        S3Config c = (S3Config) config;
        String key = objectKey(c, meta.getStorageKey());
        try (S3Client client = client(c)) {
            PutObjectRequest.Builder req = PutObjectRequest.builder()
                    .bucket(c.getBucket())
                    .key(key);
            if (meta.getContentType() != null) {
                req.contentType(meta.getContentType());
            }
            client.putObject(req.build(), RequestBody.fromInputStream(in, meta.getSize()));
        } catch (Exception e) {
            throw new BizException("S3 上传失败: " + e.getMessage());
        }
        return UploadResult.builder()
                .storageKey(key)
                .url(joinUrl(urlPrefix, key))
                .build();
    }

    @Override
    public void delete(StorageConfig config, String storageKey) {
        S3Config c = (S3Config) config;
        try (S3Client client = client(c)) {
            client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(c.getBucket())
                    .key(storageKey)
                    .build());
        } catch (Exception e) {
            throw new BizException("S3 删除失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream download(StorageConfig config, String storageKey) {
        S3Config c = (S3Config) config;
        // 注意：S3Client 不能在这里 try-with-resources，否则关闭后流不可读。
        // 由调用方负责关闭返回的 ResponseInputStream（内部持有 client 引用）。
        S3Client client = client(c);
        try {
            return client.getObject(GetObjectRequest.builder()
                    .bucket(c.getBucket())
                    .key(storageKey)
                    .build());
        } catch (Exception e) {
            client.close();
            throw new BizException("S3 下载失败: " + e.getMessage());
        }
    }

    private S3Client client(S3Config c) {
        var builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(c.getAccessKey(), c.getSecretKey())));
        if (StringUtils.hasText(c.getRegion())) {
            builder.region(Region.of(c.getRegion()));
        }
        if (StringUtils.hasText(c.getEndpoint())) {
            builder.endpointOverride(URI.create(c.getEndpoint()));
        }
        if (Boolean.TRUE.equals(c.getPathStyleAccess())) {
            builder.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        return builder.build();
    }

    private String objectKey(S3Config c, String storageKey) {
        if (c.getBasePath() == null || c.getBasePath().isBlank()) {
            return storageKey;
        }
        return c.getBasePath().replaceAll("/+$", "") + "/" + storageKey.replaceAll("^/+", "");
    }

}
