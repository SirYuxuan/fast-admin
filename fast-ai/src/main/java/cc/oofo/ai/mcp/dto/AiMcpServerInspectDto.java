package cc.oofo.ai.mcp.dto;

import java.util.List;
import java.util.Map;

import cc.oofo.ai.mcp.entity.AiMcpServer;

/**
 * MCP 服务运行时详情。
 */
public record AiMcpServerInspectDto(
        AiMcpServer server,
        RuntimeInfo runtime,
        List<ToolInfo> tools,
        List<PromptInfo> prompts,
        List<ResourceInfo> resources) {

    public record RuntimeInfo(
            Boolean connected,
            String statusMessage,
            Integer toolCount,
            Integer promptCount,
            Integer resourceCount,
            Integer contextTokenCount,
            String instructions,
            Map<String, Object> serverInfo,
            Map<String, Object> capabilities) {
    }

    public record ToolInfo(
            String name,
            String title,
            String description,
            Map<String, Object> inputSchema,
            Map<String, Object> outputSchema) {
    }

    public record PromptInfo(
            String name,
            String title,
            String description,
            List<PromptArgumentInfo> arguments) {
    }

    public record PromptArgumentInfo(
            String name,
            String description,
            Boolean required) {
    }

    public record ResourceInfo(
            String uri,
            String name,
            String title,
            String description,
            String mimeType,
            Long size) {
    }
}
