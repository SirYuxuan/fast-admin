package cc.oofo.ai.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.ai.model.dto.AiModelConfigDto;
import cc.oofo.ai.model.dto.AiModelConfigSaveDto;
import cc.oofo.ai.model.dto.AiModelTestResultDto;
import cc.oofo.ai.model.entity.AiModelConfig;
import cc.oofo.ai.model.entity.query.AiModelConfigQuery;
import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import lombok.RequiredArgsConstructor;

/**
 * AI 模型配置服务。
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AiModelConfigService extends BaseService<AiModelConfig> {

    private static final String MASK = "******";
    private static final Set<String> PROVIDERS = Set.of("anthropic", "openai", "openai-compatible");

    private final AiModelProbeService probeService;

    public Page<AiModelConfigDto> page(AiModelConfigQuery query) {
        query.getQueryWrapper().orderByDesc("active").orderByDesc("created_at");
        Page<AiModelConfig> page = page(query.getMPPage(), query.getQueryWrapper());
        Page<AiModelConfigDto> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDto).toList());
        return dtoPage;
    }

    public AiModelConfigDto detail(String id) {
        return toDto(getByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public AiModelConfig getActiveEnabled() {
        return getOne(new LambdaQueryWrapper<AiModelConfig>()
                .eq(AiModelConfig::getActive, true)
                .eq(AiModelConfig::getEnabled, true)
                .last("limit 1"));
    }

    public void add(AiModelConfigSaveDto dto) {
        validate(dto, null);
        if (nameExists(null, dto.getName())) {
            throw new BizException("模型配置名称已存在");
        }

        AiModelConfig entity = new AiModelConfig();
        copyToEntity(dto, entity);
        entity.setEnabled(dto.getEnabled() == null || dto.getEnabled());
        entity.setActive(Boolean.TRUE.equals(dto.getActive()));
        save(entity);

        if (Boolean.TRUE.equals(entity.getActive())) {
            activate(entity.getId());
        }
    }

    public void update(AiModelConfigSaveDto dto) {
        AiModelConfig entity = getByIdOrThrow(dto.getId());
        validate(dto, entity.getId());
        if (nameExists(entity.getId(), dto.getName())) {
            throw new BizException("模型配置名称已存在");
        }

        copyToEntity(dto, entity);
        updateById(entity);

        if (Boolean.TRUE.equals(dto.getActive())) {
            activate(entity.getId());
        }
    }

    public void activate(String id) {
        AiModelConfig target = getByIdOrThrow(id);
        if (!Boolean.TRUE.equals(target.getEnabled())) {
            throw new BizException("禁用的模型配置不能设为当前模型");
        }
        update(new LambdaUpdateWrapper<AiModelConfig>().set(AiModelConfig::getActive, false));
        update(new LambdaUpdateWrapper<AiModelConfig>()
                .set(AiModelConfig::getActive, true)
                .eq(AiModelConfig::getId, id));
    }

    public void changeEnabled(String id, boolean enabled) {
        AiModelConfig entity = getByIdOrThrow(id);
        entity.setEnabled(enabled);
        updateById(entity);
    }

    public void del(String id) {
        AiModelConfig entity = getByIdOrThrow(id);
        if (Boolean.TRUE.equals(entity.getActive())) {
            throw new BizException("当前激活模型不能删除");
        }
        removeById(id);
    }

    /**
     * 根据填写的提供方、链接、密钥拉取可用模型列表。
     */
    @Transactional(readOnly = true)
    public List<String> fetchModels(AiModelConfigSaveDto dto) {
        validateProvider(dto);
        return probeService.fetchModels(dto.getProvider(), dto.getBaseUrl(), resolveApiKey(dto));
    }

    /**
     * 测试模型连通性并返回延时，已存在的配置会记录本次测试结果。
     */
    public AiModelTestResultDto test(AiModelConfigSaveDto dto) {
        validateProvider(dto);
        if (!StringUtils.hasText(dto.getModel())) {
            throw new BizException("模型名称不能为空");
        }
        String apiKey = resolveApiKey(dto);

        AiModelTestResultDto result = new AiModelTestResultDto();
        try {
            long latency = probeService.test(dto.getProvider(), dto.getBaseUrl(), apiKey, dto.getModel());
            result.setSuccess(true);
            result.setLatencyMs(latency);
            recordTestResult(dto.getId(), latency, true);
            return result;
        } catch (RuntimeException e) {
            recordTestResult(dto.getId(), null, false);
            throw e;
        }
    }

    private void recordTestResult(String id, Long latency, boolean ok) {
        if (!StringUtils.hasText(id)) {
            return;
        }
        update(new LambdaUpdateWrapper<AiModelConfig>()
                .set(AiModelConfig::getLastLatencyMs, latency)
                .set(AiModelConfig::getLastTestOk, ok)
                .set(AiModelConfig::getLastTestedAt, LocalDateTime.now())
                .eq(AiModelConfig::getId, id));
    }

    /**
     * 校验提供方合法，并优先使用页面填写的密钥，缺省时回退到已存储的密钥。
     */
    private String resolveApiKey(AiModelConfigSaveDto dto) {
        if (StringUtils.hasText(dto.getApiKey()) && !MASK.equals(dto.getApiKey())) {
            return dto.getApiKey();
        }
        if (StringUtils.hasText(dto.getId())) {
            String stored = getByIdOrThrow(dto.getId()).getApiKey();
            if (StringUtils.hasText(stored)) {
                return stored;
            }
        }
        throw new BizException("API Key 不能为空");
    }

    private void validateProvider(AiModelConfigSaveDto dto) {
        if (dto == null) {
            throw new BizException("模型配置不能为空");
        }
        if (!StringUtils.hasText(dto.getProvider()) || !PROVIDERS.contains(dto.getProvider())) {
            throw new BizException("模型提供方不支持");
        }
    }

    private AiModelConfig getByIdOrThrow(String id) {
        AiModelConfig entity = getById(id);
        if (entity == null) {
            throw new BizException("模型配置不存在");
        }
        return entity;
    }

    private void validate(AiModelConfigSaveDto dto, String id) {
        if (dto == null) {
            throw new BizException("模型配置不能为空");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new BizException("模型配置名称不能为空");
        }
        if (!StringUtils.hasText(dto.getProvider()) || !PROVIDERS.contains(dto.getProvider())) {
            throw new BizException("模型提供方不支持");
        }
        if (!StringUtils.hasText(dto.getModel())) {
            throw new BizException("模型名称不能为空");
        }
        if (!StringUtils.hasText(id) && !StringUtils.hasText(dto.getApiKey())) {
            throw new BizException("API Key 不能为空");
        }
    }

    private boolean nameExists(String excludeId, String name) {
        LambdaQueryWrapper<AiModelConfig> wrapper = new LambdaQueryWrapper<AiModelConfig>().eq(AiModelConfig::getName,
                name);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(AiModelConfig::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    private void copyToEntity(AiModelConfigSaveDto dto, AiModelConfig entity) {
        entity.setName(dto.getName());
        entity.setProvider(dto.getProvider());
        entity.setModel(dto.getModel());
        entity.setBaseUrl(dto.getBaseUrl());
        if (StringUtils.hasText(dto.getApiKey()) && !MASK.equals(dto.getApiKey())) {
            entity.setApiKey(dto.getApiKey());
        }
        if (dto.getEnabled() != null) {
            entity.setEnabled(dto.getEnabled());
        }
        entity.setActive(Boolean.TRUE.equals(dto.getActive()));
        entity.setTemperature(dto.getTemperature());
        entity.setMaxTokens(dto.getMaxTokens());
        entity.setRemark(dto.getRemark());
    }

    private AiModelConfigDto toDto(AiModelConfig entity) {
        AiModelConfigDto dto = new AiModelConfigDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setProvider(entity.getProvider());
        dto.setModel(entity.getModel());
        dto.setBaseUrl(entity.getBaseUrl());
        dto.setApiKey(StringUtils.hasText(entity.getApiKey()) ? MASK : null);
        dto.setEnabled(entity.getEnabled());
        dto.setActive(entity.getActive());
        dto.setTemperature(entity.getTemperature());
        dto.setMaxTokens(entity.getMaxTokens());
        dto.setRemark(entity.getRemark());
        dto.setLastLatencyMs(entity.getLastLatencyMs());
        dto.setLastTestOk(entity.getLastTestOk());
        if (entity.getLastTestedAt() != null) {
            dto.setLastTestedAt(entity.getLastTestedAt().toString());
        }
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().toString());
        }
        return dto;
    }
}
