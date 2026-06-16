package cc.oofo.ai.tool.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.oofo.ai.agent.dto.AiChatSseEvent;
import cc.oofo.ai.agent.entity.AiToolCallLog;
import cc.oofo.ai.agent.mapper.AiToolCallLogMapper;
import cc.oofo.ai.tool.entity.AiToolConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 将后台启用的工具配置转换为 Spring AI ToolCallback，同时挂载事件通知与审计日志。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiToolCallbackService {

    private static final String INPUT_SCHEMA = """
            {"type":"object","additionalProperties":true}
            """;
    private static final int RESULT_MAX_CHARS = 4000;

    private final AiToolConfigService toolConfigService;
    private final AiToolExecutionService toolExecutionService;
    private final AiToolCallLogMapper toolCallLogMapper;
    private final ObjectMapper objectMapper;

    /**
     * 返回所有已启用工具的 ToolCallback 列表。
     *
     * @param sessionId   当前对话会话 ID，用于审计日志
     * @param operatorId  当前操作用户 ID，用于审计日志
     * @param eventSink   SSE 事件回调，工具调用前后发送 tool 事件帧；可为 null（跳过通知）
     */
    public List<ToolCallback> listEnabledCallbacks(String sessionId, String operatorId,
            Consumer<AiChatSseEvent> eventSink) {
        return toolConfigService.listEnabled().stream()
                .map(tool -> toCallback(tool, sessionId, operatorId, eventSink))
                .toList();
    }

    private ToolCallback toCallback(AiToolConfig tool, String sessionId, String operatorId,
            Consumer<AiChatSseEvent> eventSink) {
        return FunctionToolCallback
                .builder(tool.getToolCode(), (Map<String, Object> input, ToolContext context) -> {
                    String argsJson = toJson(input);
                    notify(eventSink, AiChatSseEvent.toolStart(tool.getToolCode(), argsJson));
                    long start = System.currentTimeMillis();
                    try {
                        String result = toolExecutionService.execute(
                                tool.getToolCode(), input, getPermissionCodes(context));
                        long cost = System.currentTimeMillis() - start;
                        notify(eventSink, AiChatSseEvent.toolEnd(tool.getToolCode(), true));
                        writeAuditLog(sessionId, operatorId, tool.getToolCode(),
                                argsJson, truncate(result), true, null, cost);
                        return result;
                    } catch (Exception e) {
                        long cost = System.currentTimeMillis() - start;
                        notify(eventSink, AiChatSseEvent.toolEnd(tool.getToolCode(), false));
                        writeAuditLog(sessionId, operatorId, tool.getToolCode(),
                                argsJson, null, false, e.getMessage(), cost);
                        throw e;
                    }
                })
                .description(tool.getDescription())
                .inputSchema(INPUT_SCHEMA)
                .inputType(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .build();
    }

    private void notify(Consumer<AiChatSseEvent> eventSink, AiChatSseEvent event) {
        if (eventSink != null) {
            try {
                eventSink.accept(event);
            } catch (Exception e) {
                log.debug("Failed to send tool SSE event", e);
            }
        }
    }

    private void writeAuditLog(String sessionId, String operatorId, String toolName,
            String argsJson, String resultJson, boolean success, String errorMsg, long costMs) {
        try {
            AiToolCallLog log = new AiToolCallLog();
            log.setSessionId(sessionId);
            log.setOperatorId(operatorId);
            log.setToolName(toolName);
            log.setSource("builtin");
            log.setArgumentsJson(argsJson);
            log.setResultJson(resultJson);
            log.setSuccess(success);
            log.setErrorMsg(StringUtils.hasText(errorMsg) ? truncate(errorMsg) : null);
            log.setCostMs(costMs);
            toolCallLogMapper.insert(log);
        } catch (Exception e) {
            AiToolCallbackService.log.warn("Failed to write tool call audit log for '{}': {}", toolName, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<String> getPermissionCodes(ToolContext context) {
        if (context == null) {
            return List.of();
        }
        Object value = context.getContext().get(AiToolExecutionService.TOOL_CONTEXT_PERMISSIONS);
        if (value instanceof Collection<?> collection) {
            return (Collection<String>) collection;
        }
        return List.of();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String truncate(String value) {
        if (value == null || value.length() <= RESULT_MAX_CHARS) {
            return value;
        }
        return value.substring(0, RESULT_MAX_CHARS);
    }
}
