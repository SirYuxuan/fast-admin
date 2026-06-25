package cc.oofo.ai.agent.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cc.oofo.ai.agent.dto.AiChatRequest;
import cc.oofo.ai.agent.dto.AiChatSseEvent;
import cc.oofo.ai.mcp.service.AiMcpClientManager;
import cc.oofo.ai.model.entity.AiModelConfig;
import cc.oofo.ai.model.service.AiModelConfigService;
import cc.oofo.ai.tool.service.AiToolCallbackService;
import cc.oofo.ai.tool.service.AiToolExecutionService;
import cc.oofo.framework.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 根据数据库当前模型、已启用工具和 MCP 服务器创建 ChatClient。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatClientFactory {

    private static final String MODE_AUTO = "auto";
    private static final String MODE_MANUAL = "manual";
    private static final String MODE_OFF = "off";

    private final AiModelConfigService modelConfigService;
    private final AiToolCallbackService toolCallbackService;
    private final AiMcpClientManager mcpClientManager;
    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    /**
     * 创建 ChatClient，合并数据库工具和 MCP 工具，并挂载事件通知与审计。
     *
     * @param permissionCodes 当前用户权限码列表，用于工具权限校验
     * @param sessionId       当前会话 ID，用于审计日志
     * @param operatorId      当前操作用户 ID，用于审计日志
     * @param eventSink       SSE 事件回调，工具调用前后推送 tool 事件帧
     */
    public ChatClient create(List<String> permissionCodes, String sessionId, String operatorId,
            AiChatRequest request,
            Consumer<AiChatSseEvent> eventSink) {
        ChatClient.Builder builder = createBuilder();
        if (builder == null) {
            return null;
        }

        List<String> safePermissions = permissionCodes == null ? List.of() : permissionCodes;

        List<ToolCallback> builtinCallbacks = resolveBuiltinCallbacks(request, sessionId, operatorId,
                safePermissions, eventSink);
        List<ToolCallback> mcpCallbacks = resolveMcpCallbacks(request, eventSink);
        List<ToolCallback> allCallbacks = new ArrayList<>();
        allCallbacks.addAll(builtinCallbacks);
        allCallbacks.addAll(mcpCallbacks);
        logToolCallbacks(builtinCallbacks, mcpCallbacks, eventSink);

        return builder
                .defaultToolCallbacks(allCallbacks)
                .defaultToolContext(Map.of(
                        AiToolExecutionService.TOOL_CONTEXT_PERMISSIONS, safePermissions))
                .build();
    }

    /**
     * 内置工具默认使用 auto：挂载已启用且有权限的普通工具和只读 SQL。
     * 执行任意 SQL 这类高风险工具只在 manual 模式显式选择时才会挂载。
     */
    private List<ToolCallback> resolveBuiltinCallbacks(AiChatRequest request, String sessionId, String operatorId,
            List<String> safePermissions, Consumer<AiChatSseEvent> eventSink) {
        String mode = normalizeMode(request == null ? null : request.toolMode(), MODE_AUTO);
        if (MODE_OFF.equals(mode)) {
            return List.of();
        }
        if (MODE_MANUAL.equals(mode)) {
            List<String> selectedToolCodes = safeList(request == null ? null : request.toolCodes());
            boolean includeExecuteSql = selectedToolCodes.contains(AiToolCallbackService.EXECUTE_SQL_TOOL_CODE);
            return toolCallbackService.listEnabledCallbacks(sessionId, operatorId, safePermissions,
                    selectedToolCodes, includeExecuteSql, eventSink);
        }
        return toolCallbackService.listEnabledCallbacks(sessionId, operatorId, safePermissions, eventSink);
    }

    /**
     * MCP 默认关闭。只有前端选择 auto 或 manual 时才会把 MCP 工具暴露给模型，
     * 避免每次聊天都把全部外部工具 schema 带进上下文。
     */
    private List<ToolCallback> resolveMcpCallbacks(AiChatRequest request, Consumer<AiChatSseEvent> eventSink) {
        String mode = normalizeMode(request == null ? null : request.mcpMode(), MODE_OFF);
        if (MODE_OFF.equals(mode)) {
            return List.of();
        }
        if (MODE_MANUAL.equals(mode)) {
            return mcpClientManager.listToolCallbacks(safeList(request == null ? null : request.mcpServerIds()),
                    eventSink);
        }
        return mcpClientManager.listToolCallbacks(eventSink);
    }

    private String normalizeMode(String mode, String defaultMode) {
        if (!StringUtils.hasText(mode)) {
            return defaultMode;
        }
        String normalized = mode.trim().toLowerCase();
        return switch (normalized) {
            case MODE_AUTO, MODE_MANUAL, MODE_OFF -> normalized;
            default -> defaultMode;
        };
    }

    private List<String> safeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private void logToolCallbacks(List<ToolCallback> builtinCallbacks, List<ToolCallback> mcpCallbacks,
            Consumer<AiChatSseEvent> eventSink) {
        List<String> builtinNames = toolNames(builtinCallbacks);
        List<String> mcpNames = toolNames(mcpCallbacks);
        log.info("AI chat tools mounted: builtin={} {}, mcp={} {}",
                builtinNames.size(), builtinNames, mcpNames.size(), mcpNames);
        if (eventSink != null) {
            try {
                eventSink.accept(AiChatSseEvent.thought(
                        "已挂载工具：内置 " + builtinNames.size() + " 个，MCP " + mcpNames.size() + " 个"
                                + previewToolNames(mcpNames)));
            } catch (Exception ignored) {
                log.debug("Failed to send mounted tool summary");
            }
        }
    }

    private List<String> toolNames(List<ToolCallback> callbacks) {
        if (callbacks == null || callbacks.isEmpty()) {
            return List.of();
        }
        return callbacks.stream()
                .map(callback -> callback.getToolDefinition().name())
                .toList();
    }

    private String previewToolNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return "";
        }
        int limit = Math.min(names.size(), 8);
        String suffix = names.size() > limit ? " 等" : "";
        return "（" + String.join("、", names.subList(0, limit)) + suffix + "）";
    }

    private ChatClient.Builder createBuilder() {
        AiModelConfig activeModel = modelConfigService.getActiveEnabled();
        if (activeModel != null) {
            return ChatClient.builder(createChatModel(activeModel));
        }

        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        return builder == null ? null : builder.clone();
    }

    private ChatModel createChatModel(AiModelConfig config) {
        if (!StringUtils.hasText(config.getApiKey())) {
            throw new BizException("当前 AI 模型未配置 API Key");
        }
        if ("anthropic".equals(config.getProvider())) {
            return createAnthropicModel(config);
        }
        if ("openai".equals(config.getProvider()) || "openai-compatible".equals(config.getProvider())) {
            return createOpenAiModel(config);
        }
        throw new BizException("当前 AI 模型提供方不支持：" + config.getProvider());
    }

    private ChatModel createOpenAiModel(AiModelConfig config) {
        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder();
        options.model(config.getModel());
        options.apiKey(config.getApiKey());
        if (StringUtils.hasText(config.getBaseUrl())) {
            options.baseUrl(normalizeOpenAiBaseUrl(config.getBaseUrl()));
        }
        if (config.getTemperature() != null) {
            options.temperature(config.getTemperature());
        }
        if (config.getMaxTokens() != null) {
            options.maxTokens(config.getMaxTokens());
        }
        return OpenAiChatModel.builder()
                .options(options.build())
                .build();
    }

    private ChatModel createAnthropicModel(AiModelConfig config) {
        AnthropicChatOptions.Builder options = AnthropicChatOptions.builder();
        options.model(config.getModel());
        options.apiKey(config.getApiKey());
        if (StringUtils.hasText(config.getBaseUrl())) {
            options.baseUrl(config.getBaseUrl());
        }
        if (config.getTemperature() != null) {
            options.temperature(config.getTemperature());
        }
        if (config.getMaxTokens() != null) {
            options.maxTokens(config.getMaxTokens());
        }
        return AnthropicChatModel.builder()
                .options(options.build())
                .build();
    }

    /**
     * 规范化 OpenAI 兼容接口地址：去掉结尾斜杠并保证以 /v1 结尾。
     */
    private String normalizeOpenAiBaseUrl(String baseUrl) {
        String base = baseUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (!base.endsWith("/v1")) {
            base = base + "/v1";
        }
        return base;
    }
}
