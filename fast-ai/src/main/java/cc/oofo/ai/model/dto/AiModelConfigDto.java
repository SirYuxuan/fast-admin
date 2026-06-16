package cc.oofo.ai.model.dto;

import lombok.Data;

/**
 * AI 模型配置返回对象。
 */
@Data
public class AiModelConfigDto {

    private String id;
    private String name;
    private String provider;
    private String model;
    private String baseUrl;
    private String apiKey;
    private Boolean enabled;
    private Boolean active;
    private Double temperature;
    private Integer maxTokens;
    private String remark;
    private Long lastLatencyMs;
    private Boolean lastTestOk;
    private String lastTestedAt;
    private String createdAt;
}
