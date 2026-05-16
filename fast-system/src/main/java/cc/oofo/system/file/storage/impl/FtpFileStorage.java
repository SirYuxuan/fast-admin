package cc.oofo.system.file.storage.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Component;

import cc.oofo.framework.exception.BizException;
import cc.oofo.system.file.storage.FileMeta;
import cc.oofo.system.file.storage.FileStorage;
import cc.oofo.system.file.storage.StorageType;
import cc.oofo.system.file.storage.UploadResult;
import cc.oofo.system.file.storage.config.FtpConfig;
import cc.oofo.system.file.storage.config.StorageConfig;

/**
 * FTP 存储
 *
 * @author Sir丶雨轩
 */
@Component
public class FtpFileStorage implements FileStorage {

    @Override
    public StorageType type() {
        return StorageType.FTP;
    }

    @Override
    public Class<? extends StorageConfig> configClass() {
        return FtpConfig.class;
    }

    @Override
    public UploadResult upload(StorageConfig config, String urlPrefix, InputStream in, FileMeta meta) {
        FtpConfig c = (FtpConfig) config;
        FTPClient client = connect(c);
        try {
            String fullPath = join(c.getBasePath(), meta.getStorageKey());
            ensureDir(client, parentDir(fullPath));
            client.setFileType(FTP.BINARY_FILE_TYPE);
            if (!client.storeFile(fullPath, in)) {
                throw new BizException("FTP 上传失败: reply=" + client.getReplyString());
            }
        } catch (Exception e) {
            throw e instanceof BizException be ? be : new BizException("FTP 上传失败: " + e.getMessage());
        } finally {
            disconnect(client);
        }
        return UploadResult.builder()
                .storageKey(meta.getStorageKey())
                .url(joinUrl(urlPrefix, meta.getStorageKey()))
                .build();
    }

    @Override
    public void delete(StorageConfig config, String storageKey) {
        FtpConfig c = (FtpConfig) config;
        FTPClient client = connect(c);
        try {
            client.deleteFile(join(c.getBasePath(), storageKey));
        } catch (Exception e) {
            throw new BizException("FTP 删除失败: " + e.getMessage());
        } finally {
            disconnect(client);
        }
    }

    @Override
    public InputStream download(StorageConfig config, String storageKey) {
        FtpConfig c = (FtpConfig) config;
        FTPClient client = connect(c);
        try {
            client.setFileType(FTP.BINARY_FILE_TYPE);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            if (!client.retrieveFile(join(c.getBasePath(), storageKey), buf)) {
                throw new BizException("FTP 下载失败: reply=" + client.getReplyString());
            }
            return new ByteArrayInputStream(buf.toByteArray());
        } catch (Exception e) {
            throw e instanceof BizException be ? be : new BizException("FTP 下载失败: " + e.getMessage());
        } finally {
            disconnect(client);
        }
    }

    private FTPClient connect(FtpConfig c) {
        FTPClient client = new FTPClient();
        try {
            client.connect(c.getHost(), c.getPort() == null ? 21 : c.getPort());
            if (!client.login(c.getUsername(), c.getPassword())) {
                throw new BizException("FTP 登录失败");
            }
            if (c.getPassive() == null || c.getPassive()) {
                client.enterLocalPassiveMode();
            }
            return client;
        } catch (Exception e) {
            disconnect(client);
            throw e instanceof BizException be ? be : new BizException("FTP 连接失败: " + e.getMessage());
        }
    }

    private void disconnect(FTPClient client) {
        if (client == null) {
            return;
        }
        try {
            if (client.isConnected()) {
                client.logout();
                client.disconnect();
            }
        } catch (Exception ignored) {
        }
    }

    /** 创建多级目录（FTP 多数实现没有 mkdir -p） */
    private void ensureDir(FTPClient client, String dir) throws java.io.IOException {
        if (dir == null || dir.isBlank() || "/".equals(dir)) {
            return;
        }
        String[] parts = dir.split("/");
        StringBuilder path = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) {
                continue;
            }
            path.append("/").append(p);
            if (!client.changeWorkingDirectory(path.toString())) {
                client.makeDirectory(path.toString());
            }
        }
    }

    private String join(String base, String key) {
        String b = base == null ? "" : base.replaceAll("/+$", "");
        String k = key == null ? "" : key.replaceAll("^/+", "");
        return b + "/" + k;
    }

    private String parentDir(String path) {
        int idx = path.lastIndexOf('/');
        return idx <= 0 ? "/" : path.substring(0, idx);
    }

}
