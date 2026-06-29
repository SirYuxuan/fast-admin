package cc.oofo.ai.rag.dto;

import lombok.Data;

/**
 * AI 知识库运行期配置。
 */
@Data
public class AiRagConfigDto {

    private Boolean enabled;

    private String collectionName;

    private String qdrantUrl;

    private String qdrantApiKey;

    private Integer qdrantTimeoutMs;

    private String embeddingBaseUrl;

    private String embeddingApiKey;

    private String embeddingModel;

    private Integer embeddingTimeoutMs;
}
