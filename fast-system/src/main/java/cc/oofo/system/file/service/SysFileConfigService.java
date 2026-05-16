package cc.oofo.system.file.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.file.entity.SysFile;
import cc.oofo.system.file.entity.SysFileConfig;
import cc.oofo.system.file.mapper.SysFileMapper;
import cc.oofo.system.file.entity.dto.SysFileConfigDto;
import cc.oofo.system.file.entity.dto.SysFileConfigSaveDto;
import cc.oofo.system.file.entity.query.SysFileConfigQuery;
import cc.oofo.system.file.event.FileConfigChangedEvent;
import cc.oofo.system.file.storage.FileStorageFactory;
import cc.oofo.system.file.storage.StorageType;
import lombok.RequiredArgsConstructor;

/**
 * 文件存储配置服务
 *
 * @author Sir丶雨轩
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SysFileConfigService extends BaseService<SysFileConfig> {

    /** 密钥/密码字段：返回前端时屏蔽 */
    private static final Set<String> SECRET_KEYS = Set.of("accessKey", "secretKey", "password", "privateKey",
            "passphrase");

    private static final String MASK = "******";

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher publisher;
    private final SysFileMapper sysFileMapper;

    /** 工厂依赖 service，会形成循环 → 延迟注入 */
    @Lazy
    @Autowired
    private FileStorageFactory storageFactory;

    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysFileConfigDto> page(SysFileConfigQuery query) {
        var page = page(query.getMPPage(), query.getQueryWrapper());
        var dtoPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysFileConfigDto>(
                page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDto).toList());
        return dtoPage;
    }

    public SysFileConfig getActiveOrThrow() {
        SysFileConfig cfg = getOne(new LambdaQueryWrapper<SysFileConfig>().eq(SysFileConfig::getIsActive, true), false);
        if (cfg == null) {
            throw new BizException("未配置激活的文件存储");
        }
        return cfg;
    }

    public SysFileConfig getByIdOrThrow(String id) {
        SysFileConfig cfg = getById(id);
        if (cfg == null) {
            throw new BizException("配置不存在");
        }
        return cfg;
    }

    public void add(SysFileConfigSaveDto dto) {
        validateType(dto.getType());
        if (nameExists(null, dto.getName())) {
            throw new BizException("配置名已存在");
        }
        validateConfigJson(dto.getType(), dto.getConfig());

        SysFileConfig entity = new SysFileConfig();
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setConfig(toJson(dto.getConfig()));
        entity.setUrlPrefix(stripTrailingSlash(dto.getUrlPrefix()));
        entity.setRemark(dto.getRemark());
        entity.setIsActive(false);
        save(entity);
    }

    public void update(SysFileConfigSaveDto dto) {
        SysFileConfig existing = getByIdOrThrow(dto.getId());
        if (!existing.getType().equals(dto.getType())) {
            throw new BizException("不允许修改存储类型（建议新建配置）");
        }
        if (nameExists(dto.getId(), dto.getName())) {
            throw new BizException("配置名已存在");
        }

        // 合并：前端传过来的密钥若是脱敏占位符，则保留原值
        String mergedConfig = mergeMaskedSecrets(existing.getConfig(), toJson(dto.getConfig()));
        validateConfigJson(dto.getType(), parseAsMap(mergedConfig));

        SysFileConfig u = new SysFileConfig();
        u.setId(dto.getId());
        u.setName(dto.getName());
        u.setConfig(mergedConfig);
        u.setUrlPrefix(stripTrailingSlash(dto.getUrlPrefix()));
        u.setRemark(dto.getRemark());
        updateById(u);

        if (Boolean.TRUE.equals(existing.getIsActive())) {
            publisher.publishEvent(new FileConfigChangedEvent(existing.getId()));
        }
    }

    public void activate(String id) {
        SysFileConfig target = getByIdOrThrow(id);
        if (Boolean.TRUE.equals(target.getIsActive())) {
            return;
        }
        update(new LambdaUpdateWrapper<SysFileConfig>()
                .set(SysFileConfig::getIsActive, false)
                .eq(SysFileConfig::getIsActive, true));
        update(new LambdaUpdateWrapper<SysFileConfig>()
                .set(SysFileConfig::getIsActive, true)
                .eq(SysFileConfig::getId, id));
        publisher.publishEvent(new FileConfigChangedEvent(id));
    }

    public void del(String id) {
        SysFileConfig cfg = getByIdOrThrow(id);
        if (Boolean.TRUE.equals(cfg.getIsActive())) {
            throw new BizException("激活中的配置不能删除");
        }
        Long refCount = sysFileMapper.selectCount(
                new LambdaQueryWrapper<SysFile>().eq(SysFile::getConfigId, id));
        if (refCount != null && refCount > 0) {
            throw new BizException("该配置已被 " + refCount + " 个文件引用，不能删除");
        }
        removeById(id);
    }

    public SysFileConfigDto detail(String id) {
        return toDto(getByIdOrThrow(id));
    }

    // ---------- 内部辅助 ----------

    private boolean nameExists(String excludeId, String name) {
        LambdaQueryWrapper<SysFileConfig> q = new LambdaQueryWrapper<SysFileConfig>().eq(SysFileConfig::getName, name);
        if (StringUtils.hasText(excludeId)) {
            q.ne(SysFileConfig::getId, excludeId);
        }
        return count(q) > 0;
    }

    private void validateType(String type) {
        try {
            StorageType.valueOf(type);
        } catch (Exception e) {
            throw new BizException("不支持的存储类型: " + type);
        }
    }

    private void validateConfigJson(String type, Object config) {
        if (config == null) {
            throw new BizException("配置参数不能为空");
        }
        // 由工厂复用现有的类型化反序列化做格式校验
        storageFactory.parseConfig(StorageType.valueOf(type), toJson(config));
    }

    private String toJson(Object obj) {
        try {
            return obj instanceof String s ? s : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new BizException("序列化配置失败: " + e.getMessage());
        }
    }

    private Map<String, Object> parseAsMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new BizException("解析配置失败: " + e.getMessage());
        }
    }

    private String mergeMaskedSecrets(String oldJson, String newJson) {
        Map<String, Object> oldMap = parseAsMap(oldJson);
        Map<String, Object> newMap = parseAsMap(newJson);
        for (String k : SECRET_KEYS) {
            Object v = newMap.get(k);
            if (v == null || MASK.equals(v) || (v instanceof String s && s.isBlank())) {
                if (oldMap.containsKey(k)) {
                    newMap.put(k, oldMap.get(k));
                }
            }
        }
        return toJson(newMap);
    }

    private SysFileConfigDto toDto(SysFileConfig entity) {
        SysFileConfigDto dto = new SysFileConfigDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setUrlPrefix(entity.getUrlPrefix());
        dto.setIsActive(entity.getIsActive());
        dto.setRemark(entity.getRemark());
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().toString());
        }
        Map<String, Object> masked = new HashMap<>(parseAsMap(entity.getConfig()));
        for (String k : SECRET_KEYS) {
            if (masked.get(k) instanceof String s && !s.isBlank()) {
                masked.put(k, MASK);
            }
        }
        dto.setConfig(masked);
        return dto;
    }

    private String stripTrailingSlash(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("/+$", "");
    }

    public List<SysFileConfig> listAll() {
        return list();
    }

}
