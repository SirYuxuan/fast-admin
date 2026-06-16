package cc.oofo.ai.mcp.dto;

import lombok.Data;

/**
 * MCP 服务配置保存对象。
 */
@Data
public class AiMcpServerSaveDto {

    private String id;
    private String name;
    private String transport;
    private String command;
    private String url;
    private String argsJson;
    private String headersJson;
    private Boolean enabled;
    private String remark;
}
