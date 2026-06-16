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

import cc.oofo.ai.agent.dto.AiChatSseEvent;
import cc.oofo.ai.mcp.service.AiMcpClientManager;
import cc.oofo.ai.model.entity.AiModelConfig;
import cc.oofo.ai.model.service.AiModelConfigService;
import cc.oofo.ai.tool.service.AiToolCallbackService;
import cc.oofo.ai.tool.service.AiToolExecutionService;
import cc.oofo.framework.exception.BizException;
import lombok.RequiredArgsConstructor;

/**
 * 根据数据库当前模型、已启用工具和 MCP 服务器创建 ChatClient。
 */
@Service
@RequiredArgsConstructor
public class AiChatClientFactory {

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
            Consumer<AiChatSseEvent> eventSink) {
        ChatClient.Builder builder = createBuilder();
        if (builder == null) {
            return null;
        }

        List<String> safePermissions = permissionCodes == null ? List.of() : permissionCodes;

        List<ToolCallback> allCallbacks = new ArrayList<>();
        allCallbacks.addAll(toolCallbackService.listEnabledCallbacks(sessionId, operatorId, eventSink));
        allCallbacks.addAll(mcpClientManager.listToolCallbacks());

        return builder
                .defaultToolCallbacks(allCallbacks)
                .defaultToolContext(Map.of(
                        AiToolExecutionService.TOOL_CONTEXT_PERMISSIONS, safePermissions))
                .build();
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
