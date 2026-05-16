package cc.oofo.system.file.storage.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import cc.oofo.framework.exception.BizException;
import cc.oofo.system.file.storage.FileMeta;
import cc.oofo.system.file.storage.FileStorage;
import cc.oofo.system.file.storage.StorageType;
import cc.oofo.system.file.storage.UploadResult;
import cc.oofo.system.file.storage.config.SftpConfig;
import cc.oofo.system.file.storage.config.StorageConfig;

/**
 * SFTP 存储
 *
 * @author Sir丶雨轩
 */
@Component
public class SftpFileStorage implements FileStorage {

    @Override
    public StorageType type() {
        return StorageType.SFTP;
    }

    @Override
    public Class<? extends StorageConfig> configClass() {
        return SftpConfig.class;
    }

    @Override
    public UploadResult upload(StorageConfig config, String urlPrefix, InputStream in, FileMeta meta) {
        SftpConfig c = (SftpConfig) config;
        Holder h = open(c);
        try {
            String full = join(c.getBasePath(), meta.getStorageKey());
            ensureDir(h.sftp, parentDir(full));
            h.sftp.put(in, full);
        } catch (Exception e) {
            throw new BizException("SFTP 上传失败: " + e.getMessage());
        } finally {
            close(h);
        }
        return UploadResult.builder()
                .storageKey(meta.getStorageKey())
                .url(joinUrl(urlPrefix, meta.getStorageKey()))
                .build();
    }

    @Override
    public void delete(StorageConfig config, String storageKey) {
        SftpConfig c = (SftpConfig) config;
        Holder h = open(c);
        try {
            h.sftp.rm(join(c.getBasePath(), storageKey));
        } catch (SftpException e) {
            // 文件不存在视为删除成功
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return;
            }
            throw new BizException("SFTP 删除失败: " + e.getMessage());
        } finally {
            close(h);
        }
    }

    @Override
    public InputStream download(StorageConfig config, String storageKey) {
        SftpConfig c = (SftpConfig) config;
        Holder h = open(c);
        try (InputStream remote = h.sftp.get(join(c.getBasePath(), storageKey))) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            remote.transferTo(buf);
            return new ByteArrayInputStream(buf.toByteArray());
        } catch (Exception e) {
            throw new BizException("SFTP 下载失败: " + e.getMessage());
        } finally {
            close(h);
        }
    }

    private record Holder(Session session, ChannelSftp sftp) {
    }

    private Holder open(SftpConfig c) {
        Session session = null;
        ChannelSftp sftp = null;
        try {
            JSch jsch = new JSch();
            if (StringUtils.hasText(c.getPrivateKey())) {
                byte[] keyBytes = c.getPrivateKey().getBytes();
                byte[] passBytes = StringUtils.hasText(c.getPassphrase()) ? c.getPassphrase().getBytes() : null;
                jsch.addIdentity("sftp-key", keyBytes, null, passBytes);
            }
            session = jsch.getSession(c.getUsername(), c.getHost(), c.getPort() == null ? 22 : c.getPort());
            if (StringUtils.hasText(c.getPassword())) {
                session.setPassword(c.getPassword());
            }
            Properties cfg = new Properties();
            cfg.put("StrictHostKeyChecking", "no");
            session.setConfig(cfg);
            session.connect();
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
            return new Holder(session, sftp);
        } catch (Exception e) {
            close(new Holder(session, sftp));
            throw new BizException("SFTP 连接失败: " + e.getMessage());
        }
    }

    private void close(Holder h) {
        if (h == null) {
            return;
        }
        if (h.sftp != null) {
            try {
                h.sftp.disconnect();
            } catch (Exception ignored) {
            }
        }
        if (h.session != null) {
            try {
                h.session.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    /** 递归创建远端目录 */
    private void ensureDir(ChannelSftp sftp, String dir) throws SftpException {
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
            try {
                sftp.cd(path.toString());
            } catch (SftpException e) {
                sftp.mkdir(path.toString());
                sftp.cd(path.toString());
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
