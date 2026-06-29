package cc.oofo.ai.rag.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import cc.oofo.ai.rag.dto.AiRagConfigDto;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.config.entity.SysConfig;
import cc.oofo.system.config.service.SysConfigService;
import lombok.RequiredArgsConstructor;

/**
 * RAG 运行期配置，只从系统参数读取。
 */
@Service
@RequiredArgsConstructor
public class AiRagConfigService {

    private static final String ENABLED = "ai.rag.enabled";
    private static final String COLLECTION_NAME = "ai.rag.collection-name";
    private static final String QDRANT_URL = "ai.rag.qdrant.url";
    private static final String QDRANT_API_KEY = "ai.rag.qdrant.api-key";
    private static final String QDRANT_TIMEOUT_MS = "ai.rag.qdrant.timeout-ms";
    private static final String EMBEDDING_BASE_URL = "ai.rag.embedding.base-url";
    private static final String EMBEDDING_API_KEY = "ai.rag.embedding.api-key";
    private static final String EMBEDDING_MODEL = "ai.rag.embedding.model";
    private static final String EMBEDDING_TIMEOUT_MS = "ai.rag.embedding.timeout-ms";

    private static final boolean DEFAULT_ENABLED = true;
    private static final String DEFAULT_COLLECTION_NAME = "fast_admin_rag";
    private static final String DEFAULT_QDRANT_URL = "";
    private static final String DEFAULT_QDRANT_API_KEY = "";
    private static final int DEFAULT_QDRANT_TIMEOUT_MS = 5000;
    private static final String DEFAULT_EMBEDDING_BASE_URL = "";
    private static final String DEFAULT_EMBEDDING_API_KEY = "";
    private static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-3-small";
    private static final int DEFAULT_EMBEDDING_TIMEOUT_MS = 20000;

    private static final int MIN_TIMEOUT_MS = 1000;
    private static final int MAX_TIMEOUT_MS = 120_000;

    private final SysConfigService sysConfigService;

    public AiRagConfigDto current() {
        AiRagConfigDto dto = new AiRagConfigDto();
        dto.setEnabled(isEnabled());
        dto.setCollectionName(getCollectionName());
        dto.setQdrantUrl(getQdrantUrl());
        dto.setQdrantApiKey(getQdrantApiKey());
        dto.setQdrantTimeoutMs(getQdrantTimeoutMs());
        dto.setEmbeddingBaseUrl(getEmbeddingBaseUrl());
        dto.setEmbeddingApiKey(getEmbeddingApiKey());
        dto.setEmbeddingModel(getEmbeddingModel());
        dto.setEmbeddingTimeoutMs(getEmbeddingTimeoutMs());
        return dto;
    }

    @Transactional
    public void save(AiRagConfigDto dto) {
        if (dto == null) {
            throw new BizException("AI 配置不能为空");
        }
        if (!StringUtils.hasText(dto.getCollectionName())) {
            throw new BizException("Qdrant 集合名不能为空");
        }
        if (!StringUtils.hasText(dto.getQdrantUrl())) {
            throw new BizException("Qdrant URL 不能为空");
        }
        if (!StringUtils.hasText(dto.getEmbeddingModel())) {
            throw new BizException("Embedding 模型不能为空");
        }

        upsert(ENABLED, "AI 知识库开关", String.valueOf(!Boolean.FALSE.equals(dto.getEnabled())),
                "控制 AI 知识库 / RAG 功能是否启用");
        upsert(COLLECTION_NAME, "AI 知识库 Qdrant 集合名", dto.getCollectionName().trim(),
                "AI 知识库写入 Qdrant 时使用的集合名");
        upsert(QDRANT_URL, "AI 知识库 Qdrant URL", dto.getQdrantUrl().trim(),
                "Qdrant REST 地址，例如 http://127.0.0.1:6333");
        upsert(QDRANT_API_KEY, "AI 知识库 Qdrant API Key", trimToEmpty(dto.getQdrantApiKey()),
                "Qdrant API Key，未开启鉴权时留空");
        upsert(QDRANT_TIMEOUT_MS, "AI 知识库 Qdrant 超时",
                String.valueOf(clampTimeout(dto.getQdrantTimeoutMs(), DEFAULT_QDRANT_TIMEOUT_MS)),
                "Qdrant 请求超时时间，单位毫秒");
        upsert(EMBEDDING_BASE_URL, "AI 知识库 Embedding Base URL", trimToEmpty(dto.getEmbeddingBaseUrl()),
                "OpenAI 兼容 Embedding Base URL，例如 https://api.openai.com/v1");
        upsert(EMBEDDING_API_KEY, "AI 知识库 Embedding API Key", trimToEmpty(dto.getEmbeddingApiKey()),
                "Embedding 服务 API Key");
        upsert(EMBEDDING_MODEL, "AI 知识库 Embedding 模型", dto.getEmbeddingModel().trim(),
                "Embedding 模型名称，例如 text-embedding-3-small");
        upsert(EMBEDDING_TIMEOUT_MS, "AI 知识库 Embedding 超时", String.valueOf(clampTimeout(
                dto.getEmbeddingTimeoutMs(), DEFAULT_EMBEDDING_TIMEOUT_MS)),
                "Embedding 请求超时时间，单位毫秒");
    }

    public boolean isEnabled() {
        return getBoolean(ENABLED, DEFAULT_ENABLED);
    }

    public String getCollectionName() {
        return getString(COLLECTION_NAME, DEFAULT_COLLECTION_NAME);
    }

    public String getQdrantUrl() {
        return getString(QDRANT_URL, DEFAULT_QDRANT_URL);
    }

    public String getQdrantApiKey() {
        return getString(QDRANT_API_KEY, DEFAULT_QDRANT_API_KEY);
    }

    public int getQdrantTimeoutMs() {
        return getInt(QDRANT_TIMEOUT_MS, DEFAULT_QDRANT_TIMEOUT_MS);
    }

    public String getEmbeddingBaseUrl() {
        return getString(EMBEDDING_BASE_URL, DEFAULT_EMBEDDING_BASE_URL);
    }

    public String getEmbeddingApiKey() {
        return getString(EMBEDDING_API_KEY, DEFAULT_EMBEDDING_API_KEY);
    }

    public String getEmbeddingModel() {
        return getString(EMBEDDING_MODEL, DEFAULT_EMBEDDING_MODEL);
    }

    public int getEmbeddingTimeoutMs() {
        return getInt(EMBEDDING_TIMEOUT_MS, DEFAULT_EMBEDDING_TIMEOUT_MS);
    }

    private void upsert(String key, String name, String value, String remark) {
        SysConfig config = sysConfigService.query().eq("config_key", key).one();
        if (config == null) {
            config = new SysConfig();
            config.setId(key.replace('.', '_'));
            config.setConfigKey(key);
            config.setConfigName(name);
            config.setConfigType(1);
            config.setConfigValue(value);
            config.setRemark(remark);
            sysConfigService.save(config);
            return;
        }
        config.setConfigName(name);
        config.setConfigValue(value);
        config.setConfigType(1);
        config.setRemark(remark);
        sysConfigService.updateById(config);
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        String value = sysConfigService.getValue(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        return switch (value.trim().toLowerCase()) {
            case "1", "true", "yes", "on" -> true;
            case "0", "false", "no", "off" -> false;
            default -> defaultValue;
        };
    }

    private int getInt(String key, int defaultValue) {
        String value = sysConfigService.getValue(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return clampTimeout(Integer.parseInt(value.trim()), defaultValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String getString(String key, String defaultValue) {
        String value = sysConfigService.getValue(key);
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private int clampTimeout(Integer value, int defaultValue) {
        int timeout = value == null ? defaultValue : value;
        return Math.min(Math.max(timeout, MIN_TIMEOUT_MS), MAX_TIMEOUT_MS);
    }

    private String trimToEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
