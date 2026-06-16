package cc.oofo.ai.model.dto;

import lombok.Data;

/**
 * AI 模型配置保存对象。
 */
@Data
public class AiModelConfigSaveDto {

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
}
