package cc.oofo.ai.config;

import cc.oofo.ai.rag.dto.AiRagConfigDto;
import lombok.Data;

/**
 * AI 运维配置。
 */
@Data
public class AiConfigDto {

    private Boolean assistantEnabled;

    private Boolean assistantRequirePermission;

    private Integer assistantMaxToolIterations;

    private String assistantSystemPrompt;

    private Boolean mcpClientEnabled;

    private Integer chatHistoryWindow;

    private Boolean readonlySqlEnabled;

    private String readonlySqlPermissionCode;

    private Integer readonlySqlMaxRows;

    private Boolean executeSqlEnabled;

    private String executeSqlPermissionCode;

    private Integer executeSqlMaxRows;

    private Boolean schemaToolEnabled;

    private String schemaToolPermissionCode;

    private AiRagConfigDto rag;
}
