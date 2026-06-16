package cc.oofo.ai.tool.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import cc.oofo.ai.tool.entity.AiToolConfig;
import lombok.RequiredArgsConstructor;

/**
 * 将后台启用的工具配置转换为 Spring AI ToolCallback。
 */
@Service
@RequiredArgsConstructor
public class AiToolCallbackService {

    private static final String INPUT_SCHEMA = """
            {"type":"object","additionalProperties":true}
            """;

    private final AiToolConfigService toolConfigService;
    private final AiToolExecutionService toolExecutionService;

    public List<ToolCallback> listEnabledCallbacks() {
        return toolConfigService.listEnabled().stream()
                .map(this::toCallback)
                .toList();
    }

    private ToolCallback toCallback(AiToolConfig tool) {
        return FunctionToolCallback
                .builder(tool.getToolCode(), (Map<String, Object> input, ToolContext context) ->
                        toolExecutionService.execute(tool.getToolCode(), input, getPermissionCodes(context)))
                .description(tool.getDescription())
                .inputSchema(INPUT_SCHEMA)
                .inputType(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .build();
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
}
