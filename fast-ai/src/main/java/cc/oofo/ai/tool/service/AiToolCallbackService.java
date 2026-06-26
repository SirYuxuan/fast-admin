package cc.oofo.ai.tool.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.oofo.ai.agent.dto.AiChatSseEvent;
import cc.oofo.ai.agent.service.AiToolAuditLogger;
import cc.oofo.ai.agent.service.AiToolConfirmationService;
import cc.oofo.ai.config.AiAssistantSettingService;
import cc.oofo.ai.tool.entity.AiToolConfig;
import cc.oofo.framework.exception.BizException;
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
    public static final String READONLY_SQL_TOOL_CODE = "execute_readonly_sql";
    public static final String EXECUTE_SQL_TOOL_CODE = "execute_sql";
    public static final String SCHEMA_TOOL_CODE = "describe_schema";
    private static final String SCHEMA_TOOL_INPUT_SCHEMA = """
            {
              "type":"object",
              "additionalProperties":false,
              "properties":{
                "table":{"type":"string","description":"表名；留空返回当前库所有表名与注释，传入表名返回该表字段定义"}
              }
            }
            """;
    private static final String READONLY_SQL_INPUT_SCHEMA = """
            {
              "type":"object",
              "additionalProperties":false,
              "required":["sql"],
              "properties":{
                "sql":{"type":"string","description":"只读 SQL，只允许 select/show/desc/describe/explain，使用 :param 命名参数"},
                "params":{"type":"object","description":"SQL 命名参数，例如 {\\\"userId\\\":\\\"1\\\"}"}
              }
            }
            """;
    private static final String EXECUTE_SQL_INPUT_SCHEMA = """
            {
              "type":"object",
              "additionalProperties":false,
              "required":["sql"],
              "properties":{
                "sql":{"type":"string","description":"任意 SQL 语句，支持 select/insert/update/delete/ddl，使用 :param 命名参数"},
                "params":{"type":"object","description":"SQL 命名参数，例如 {\\\"userId\\\":\\\"1\\\"}"}
              }
            }
            """;
    private static final int RESULT_MAX_CHARS = 4000;

    private final AiAssistantSettingService settingService;
    private final AiToolConfigService toolConfigService;
    private final AiToolExecutionService toolExecutionService;
    private final AiToolConfirmationService confirmationService;
    private final AiToolAuditLogger auditLogger;
    private final ObjectMapper objectMapper;

    @Value("${demo.enabled:false}")
    private boolean demoModeEnabled;

    @Value("${demo.message:演示模式禁止操作}")
    private String demoModeMessage;

    /**
     * 返回当前用户有权使用的已启用工具列表。无权限的工具不会挂载给模型。
     *
     * @param sessionId       当前对话会话 ID，用于审计日志
     * @param operatorId      当前操作用户 ID，用于审计日志
     * @param permissionCodes 当前用户权限码，用于过滤无权限工具
     * @param eventSink       SSE 事件回调，工具调用前后发送 tool 事件帧；可为 null（跳过通知）
     */
    public List<ToolCallback> listEnabledCallbacks(String sessionId, String operatorId,
            Collection<String> permissionCodes, Consumer<AiChatSseEvent> eventSink) {
        return listEnabledCallbacks(sessionId, operatorId, permissionCodes, null, false, eventSink);
    }

    /**
     * 按请求选择范围返回工具回调。
     *
     * <p>selectedToolCodes 为空表示自动模式，会挂载普通已启用工具和只读 SQL；任意 SQL 工具
     * 默认不进入自动模式，必须由前端显式选择后才会暴露给模型。</p>
     */
    public List<ToolCallback> listEnabledCallbacks(String sessionId, String operatorId,
            Collection<String> permissionCodes, Collection<String> selectedToolCodes,
            boolean includeExecuteSql, Consumer<AiChatSseEvent> eventSink) {
        boolean manualMode = selectedToolCodes != null;
        List<ToolCallback> callbacks = new ArrayList<>(toolConfigService.listEnabled().stream()
                .filter(tool -> hasPermission(permissionCodes, tool.getPermissionCode()))
                .filter(tool -> !manualMode || selectedToolCodes.contains(tool.getToolCode()))
                .map(tool -> toCallback(tool, sessionId, operatorId, eventSink))
                .toList());
        if (settingService.isReadonlySqlEnabled()
                && hasPermission(permissionCodes, settingService.getReadonlySqlPermissionCode())
                && (!manualMode || selectedToolCodes.contains(READONLY_SQL_TOOL_CODE))) {
            callbacks.add(readonlySqlCallback(sessionId, operatorId, eventSink));
        }
        if (settingService.isSchemaToolEnabled()
                && hasPermission(permissionCodes, settingService.getSchemaToolPermissionCode())
                && (!manualMode || selectedToolCodes.contains(SCHEMA_TOOL_CODE))) {
            callbacks.add(schemaToolCallback(sessionId, operatorId, eventSink));
        }
        if (settingService.isExecuteSqlEnabled()
                && !demoModeEnabled
                && includeExecuteSql
                && hasPermission(permissionCodes, settingService.getExecuteSqlPermissionCode())
                && (!manualMode || selectedToolCodes.contains(EXECUTE_SQL_TOOL_CODE))) {
            callbacks.add(executeSqlCallback(sessionId, operatorId, eventSink));
        }
        return callbacks;
    }

    private boolean hasPermission(Collection<String> permissionCodes, String requiredCode) {
        if (!StringUtils.hasText(requiredCode)) {
            return true;
        }
        return permissionCodes != null && permissionCodes.contains(requiredCode);
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
                        notify(eventSink, AiChatSseEvent.toolEnd(tool.getToolCode(), "builtin", true, cost));
                        writeAuditLog(sessionId, operatorId, tool.getToolCode(),
                                argsJson, truncate(result), true, null, cost);
                        return result;
                    } catch (Exception e) {
                        long cost = System.currentTimeMillis() - start;
                        notify(eventSink, AiChatSseEvent.toolEnd(tool.getToolCode(), "builtin", false, cost));
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

    private ToolCallback readonlySqlCallback(String sessionId, String operatorId,
            Consumer<AiChatSseEvent> eventSink) {
        return FunctionToolCallback
                .builder(READONLY_SQL_TOOL_CODE, (Map<String, Object> input, ToolContext context) -> {
                    String argsJson = toJson(input);
                    notify(eventSink, AiChatSseEvent.toolStart(READONLY_SQL_TOOL_CODE, argsJson));
                    long start = System.currentTimeMillis();
                    try {
                        checkReadonlySqlPermission(getPermissionCodes(context));
                        String result = toolExecutionService.executeReadOnlySql(
                                getRequiredString(input, "sql"),
                                getParams(input),
                                settingService.getReadonlySqlMaxRows());
                        long cost = System.currentTimeMillis() - start;
                        notify(eventSink, AiChatSseEvent.toolEnd(READONLY_SQL_TOOL_CODE, "builtin", true, cost));
                        writeAuditLog(sessionId, operatorId, READONLY_SQL_TOOL_CODE,
                                argsJson, truncate(result), true, null, cost);
                        return result;
                    } catch (Exception e) {
                        long cost = System.currentTimeMillis() - start;
                        notify(eventSink, AiChatSseEvent.toolEnd(READONLY_SQL_TOOL_CODE, "builtin", false, cost));
                        writeAuditLog(sessionId, operatorId, READONLY_SQL_TOOL_CODE,
                                argsJson, null, false, e.getMessage(), cost);
                        throw e;
                    }
                })
                .description("""
                        执行一条只读 SQL 并返回 JSON 结果。仅用于查询 Fast Admin 数据库事实。
                        只允许单条 select/show/desc/describe/explain 语句；需要参数时使用 :param 命名参数并放入 params 对象。
                        """)
                .inputSchema(READONLY_SQL_INPUT_SCHEMA)
                .inputType(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .build();
    }

    private ToolCallback schemaToolCallback(String sessionId, String operatorId,
            Consumer<AiChatSseEvent> eventSink) {
        return FunctionToolCallback
                .builder(SCHEMA_TOOL_CODE, (Map<String, Object> input, ToolContext context) -> {
                    String argsJson = toJson(input);
                    notify(eventSink, AiChatSseEvent.toolStart(SCHEMA_TOOL_CODE, argsJson));
                    long start = System.currentTimeMillis();
                    try {
                        checkSchemaToolPermission(getPermissionCodes(context));
                        Object table = input == null ? null : input.get("table");
                        String result = toolExecutionService.describeSchema(
                                table == null ? null : String.valueOf(table));
                        long cost = System.currentTimeMillis() - start;
                        notify(eventSink, AiChatSseEvent.toolEnd(SCHEMA_TOOL_CODE, "builtin", true, cost));
                        writeAuditLog(sessionId, operatorId, SCHEMA_TOOL_CODE,
                                argsJson, truncate(result), true, null, cost);
                        return result;
                    } catch (Exception e) {
                        long cost = System.currentTimeMillis() - start;
                        notify(eventSink, AiChatSseEvent.toolEnd(SCHEMA_TOOL_CODE, "builtin", false, cost));
                        writeAuditLog(sessionId, operatorId, SCHEMA_TOOL_CODE,
                                argsJson, null, false, e.getMessage(), cost);
                        throw e;
                    }
                })
                .description("""
                        查询当前数据库的库表结构（仅读取 information_schema 元数据，不涉及业务数据）。
                        table 留空时返回所有表名与注释；传入表名时返回该表的字段名、类型、是否可空、主键与注释。
                        在编写 SQL 前可先用此工具确认真实表名与字段，避免猜测。
                        """)
                .inputSchema(SCHEMA_TOOL_INPUT_SCHEMA)
                .inputType(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .build();
    }

    private void checkSchemaToolPermission(Collection<String> permissionCodes) {
        String permissionCode = settingService.getSchemaToolPermissionCode();
        if (!StringUtils.hasText(permissionCode)) {
            return;
        }
        if (permissionCodes == null || !permissionCodes.contains(permissionCode)) {
            throw new BizException("无权调用 AI 查询表结构工具");
        }
    }

    private ToolCallback executeSqlCallback(String sessionId, String operatorId,
            Consumer<AiChatSseEvent> eventSink) {
        return FunctionToolCallback
                .builder(EXECUTE_SQL_TOOL_CODE, (Map<String, Object> input, ToolContext context) -> {
                    if (demoModeEnabled) {
                        throw new BizException(demoModeMessage);
                    }
                    String sql = getRequiredString(input, "sql");
                    String argsJson = toJson(input);

                    // 发送 pending 事件，等待用户在前端确认后才继续执行。
                    String confirmToken = java.util.UUID.randomUUID().toString();
                    notify(eventSink, AiChatSseEvent.toolPending(EXECUTE_SQL_TOOL_CODE, sql, confirmToken));
                    boolean confirmed = confirmationService.waitForConfirmation(confirmToken);
                    if (!confirmed) {
                        return "用户已取消 SQL 执行。";
                    }

                    notify(eventSink, AiChatSseEvent.toolStart(EXECUTE_SQL_TOOL_CODE, argsJson));
                    long start = System.currentTimeMillis();
                    try {
                        checkExecuteSqlPermission(getPermissionCodes(context));
                        String result = toolExecutionService.executeAnySql(
                                sql, getParams(input), settingService.getExecuteSqlMaxRows());
                        long cost = System.currentTimeMillis() - start;
                        notify(eventSink, AiChatSseEvent.toolEnd(EXECUTE_SQL_TOOL_CODE, "builtin", true, cost));
                        writeAuditLog(sessionId, operatorId, EXECUTE_SQL_TOOL_CODE,
                                argsJson, truncate(result), true, null, cost);
                        return result;
                    } catch (Exception e) {
                        long cost = System.currentTimeMillis() - start;
                        notify(eventSink, AiChatSseEvent.toolEnd(EXECUTE_SQL_TOOL_CODE, "builtin", false, cost));
                        writeAuditLog(sessionId, operatorId, EXECUTE_SQL_TOOL_CODE,
                                argsJson, null, false, e.getMessage(), cost);
                        throw e;
                    }
                })
                .description("""
                        执行任意 SQL 并返回结果，支持 select/insert/update/delete/ddl。
                        执行前必须先向用户展示 SQL 内容并等待用户确认，用户同意后方可执行。
                        查询语句使用 :param 命名参数并放入 params 对象。
                        """)
                .inputSchema(EXECUTE_SQL_INPUT_SCHEMA)
                .inputType(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .build();
    }

    private void checkExecuteSqlPermission(Collection<String> permissionCodes) {
        String permissionCode = settingService.getExecuteSqlPermissionCode();
        if (!StringUtils.hasText(permissionCode)) {
            return;
        }
        if (permissionCodes == null || !permissionCodes.contains(permissionCode)) {
            throw new BizException("无权调用 AI 执行 SQL 工具");
        }
    }

    private void checkReadonlySqlPermission(Collection<String> permissionCodes) {
        String permissionCode = settingService.getReadonlySqlPermissionCode();
        if (!StringUtils.hasText(permissionCode)) {
            return;
        }
        if (permissionCodes == null || !permissionCodes.contains(permissionCode)) {
            throw new BizException("无权调用 AI 只读 SQL 工具");
        }
    }

    private String getRequiredString(Map<String, Object> input, String key) {
        Object value = input == null ? null : input.get(key);
        if (!(value instanceof String text) || !StringUtils.hasText(text)) {
            throw new BizException("缺少参数：" + key);
        }
        return text;
    }

    private Map<String, Object> getParams(Map<String, Object> input) {
        if (input == null || input.get("params") == null) {
            return Map.of();
        }
        Object value = input.get("params");
        if (!(value instanceof Map<?, ?>)) {
            throw new BizException("params 必须是 JSON 对象");
        }
        return objectMapper.convertValue(value, new com.fasterxml.jackson.core.type.TypeReference<>() {
        });
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
        auditLogger.write(sessionId, operatorId, toolName, AiToolAuditLogger.SOURCE_BUILTIN,
                argsJson, resultJson, success, errorMsg, costMs);
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
