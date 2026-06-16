package cc.oofo.ai.tool.dto;

import lombok.Data;

/**
 * AI 工具配置保存参数。
 */
@Data
public class AiToolConfigSaveDto {

    private String id;

    private String name;

    private String toolCode;

    private String type;

    private String description;

    private Boolean enabled;

    private String permissionCode;

    private String method;

    private String url;

    private String headersJson;

    private String bodyTemplate;

    private String sqlText;

    private Boolean readOnly;

    private Integer timeoutMs;

    private String remark;
}
