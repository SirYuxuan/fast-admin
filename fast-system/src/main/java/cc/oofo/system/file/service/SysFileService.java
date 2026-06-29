package cc.oofo.system.file.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.ksuid.Ksuid;

import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.file.entity.SysFile;
import cc.oofo.system.file.entity.SysFileConfig;
import cc.oofo.system.file.entity.query.SysFileQuery;
import cc.oofo.system.file.spi.FileReferenceChecker;
import cc.oofo.system.file.storage.FileMeta;
import cc.oofo.system.file.storage.FileStorageFactory;
import cc.oofo.system.file.storage.StorageType;
import cc.oofo.system.file.storage.UploadResult;
import lombok.RequiredArgsConstructor;

/**
 * 文件服务
 *
 * @author Sir丶雨轩
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SysFileService extends BaseService<SysFile> {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final FileStorageFactory storageFactory;
    private final SysFileConfigService configService;
    private final ObjectProvider<FileReferenceChecker> referenceCheckers;

    public Page<SysFile> page(SysFileQuery query) {
        query.getQueryWrapper().orderByDesc("created_at");
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    public SysFile upload(MultipartFile file, String bizType, String bizId) {
        if (file == null || file.isEmpty()) {
            throw new BizException("文件不能为空");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new BizException("读取文件失败: " + e.getMessage());
        }

        String originalName = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        String ext = extractExt(originalName);
        String storageKey = buildStorageKey(ext);
        String hash = sha256(bytes);

        SysFileConfig active = storageFactory.currentEntity();
        FileMeta meta = FileMeta.builder()
                .originalName(originalName)
                .storageKey(storageKey)
                .size(file.getSize())
                .contentType(file.getContentType())
                .build();

        UploadResult result;
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            result = storageFactory.upload(in, meta);
        } catch (Exception e) {
            throw e instanceof BizException be ? be : new BizException("上传失败: " + e.getMessage());
        }

        SysFile entity = new SysFile();
        entity.setOriginalName(originalName);
        entity.setStorageKey(result.getStorageKey());
        entity.setUrl(result.getUrl());
        entity.setSize(file.getSize());
        entity.setContentType(file.getContentType());
        entity.setExt(ext);
        entity.setHash(hash);
        entity.setStorageType(active.getType());
        entity.setConfigId(active.getId());
        entity.setBizType(bizType);
        entity.setBizId(bizId);
        save(entity);
        return entity;
    }

    public InputStream download(String id) {
        SysFile f = getById(id);
        if (f == null) {
            throw new BizException("文件不存在");
        }
        SysFileConfig cfg = configService.getByIdOrThrow(f.getConfigId());
        var storage = storageFactory.get(StorageType.valueOf(cfg.getType()));
        return storage.download(storageFactory.parseConfig(StorageType.valueOf(cfg.getType()), cfg.getConfig()),
                f.getStorageKey());
    }

    public void del(String id) {
        SysFile f = getById(id);
        if (f == null) {
            throw new BizException("文件不存在");
        }
        // 被其它模块（如 AI 知识库）引用的文件禁止删除，需先解除引用
        for (FileReferenceChecker checker : referenceCheckers) {
            Optional<String> reason = checker.checkReference(id);
            if (reason.isPresent()) {
                throw new BizException(reason.get());
            }
        }
        // 先删存储后台再删数据库；存储删失败也允许继续（避免脏数据 → 但记录会孤立，留给后续巡检）
        SysFileConfig cfg = configService.getById(f.getConfigId());
        if (cfg != null) {
            try {
                var storage = storageFactory.get(StorageType.valueOf(cfg.getType()));
                storage.delete(storageFactory.parseConfig(StorageType.valueOf(cfg.getType()), cfg.getConfig()),
                        f.getStorageKey());
            } catch (Exception ignored) {
            }
        }
        removeById(id);
    }

    // ---------- helpers ----------

    private String buildStorageKey(String ext) {
        String date = LocalDate.now().format(DATE_FMT);
        String name = Ksuid.newKsuid().toString();
        return StringUtils.hasText(ext) ? date + "/" + name + "." + ext : date + "/" + name;
    }

    private String extractExt(String name) {
        int idx = name.lastIndexOf('.');
        if (idx < 0 || idx == name.length() - 1) {
            return "";
        }
        String ext = name.substring(idx + 1).toLowerCase();
        return ext.length() > 16 ? "" : ext;
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(bytes));
        } catch (Exception e) {
            return null;
        }
    }

}
