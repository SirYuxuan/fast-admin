package cc.oofo.ai.rag.dto;

import java.util.List;

import lombok.Data;

/**
 * 向量库连接状态。
 */
@Data
public class AiRagVectorStoreStatusDto {

    private boolean enabled;

    private boolean connected;

    private String url;

    private String version;

    private String status;

    private String defaultCollection;

    private boolean defaultCollectionExists;

    private List<String> collections = List.of();

    private Long latencyMs;

    private String message;
}
