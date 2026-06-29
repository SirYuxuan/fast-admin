package cc.oofo.ai.config;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cc.oofo.system.config.service.SysConfigService;
import lombok.RequiredArgsConstructor;

/**
 * AI 助手运行期配置，优先从系统参数表读取。
 */
@Service
@RequiredArgsConstructor
public class AiAssistantSettingService {

    public static final String ASSISTANT_ENABLED = "ai.assistant.enabled";
    public static final String ASSISTANT_REQUIRE_PERMISSION = "ai.assistant.require-permission";
    public static final String ASSISTANT_MAX_TOOL_ITERATIONS = "ai.assistant.max-tool-iterations";
    public static final String ASSISTANT_SYSTEM_PROMPT = "ai.assistant.system-prompt";

    public static final String READONLY_SQL_ENABLED = "ai.readonly-sql.enabled";
    public static final String READONLY_SQL_PERMISSION_CODE = "ai.readonly-sql.permission-code";
    public static final String READONLY_SQL_MAX_ROWS = "ai.readonly-sql.max-rows";

    public static final String EXECUTE_SQL_ENABLED = "ai.execute-sql.enabled";
    public static final String EXECUTE_SQL_PERMISSION_CODE = "ai.execute-sql.permission-code";
    public static final String EXECUTE_SQL_MAX_ROWS = "ai.execute-sql.max-rows";

    public static final String SCHEMA_TOOL_ENABLED = "ai.schema-tool.enabled";
    public static final String SCHEMA_TOOL_PERMISSION_CODE = "ai.schema-tool.permission-code";

    public static final String CHAT_HISTORY_WINDOW = "ai.chat.history-window";

    private static final boolean DEFAULT_ASSISTANT_ENABLED = true;
    private static final boolean DEFAULT_ASSISTANT_REQUIRE_PERMISSION = false;
    private static final int DEFAULT_ASSISTANT_MAX_TOOL_ITERATIONS = 8;
    private static final int MIN_ASSISTANT_MAX_TOOL_ITERATIONS = 1;
    private static final int MAX_ASSISTANT_MAX_TOOL_ITERATIONS = 20;
    private static final String DEFAULT_ASSISTANT_SYSTEM_PROMPT = """
            你是 Fast Admin 后台的 AI 运维助手。
            回答要简洁、准确；当你无法确认后台事实时，明确说明需要工具或数据支持。
            当前版本仅支持对话，不得声称已经执行后台写操作。
            """;

    private static final boolean DEFAULT_READONLY_SQL_ENABLED = true;
    private static final String DEFAULT_READONLY_SQL_PERMISSION_CODE = "ai:sql:readonly";
    private static final int DEFAULT_READONLY_SQL_MAX_ROWS = 100;
    private static final int MAX_READONLY_SQL_ROWS = 100;

    private static final boolean DEFAULT_EXECUTE_SQL_ENABLED = false;
    private static final String DEFAULT_EXECUTE_SQL_PERMISSION_CODE = "ai:sql:execute";
    private static final int DEFAULT_EXECUTE_SQL_MAX_ROWS = 100;
    private static final int MAX_EXECUTE_SQL_ROWS = 500;

    private static final boolean DEFAULT_SCHEMA_TOOL_ENABLED = true;
    private static final String DEFAULT_SCHEMA_TOOL_PERMISSION_CODE = "ai:sql:readonly";

    private static final int DEFAULT_CHAT_HISTORY_WINDOW = 20;
    private static final int MIN_CHAT_HISTORY_WINDOW = 2;
    private static final int MAX_CHAT_HISTORY_WINDOW = 100;

    private final SysConfigService sysConfigService;

    public boolean isAssistantEnabled() {
        return getBoolean(ASSISTANT_ENABLED, DEFAULT_ASSISTANT_ENABLED);
    }

    public boolean isAssistantRequirePermission() {
        return getBoolean(ASSISTANT_REQUIRE_PERMISSION, DEFAULT_ASSISTANT_REQUIRE_PERMISSION);
    }

    public int getAssistantMaxToolIterations() {
        int value = getInt(ASSISTANT_MAX_TOOL_ITERATIONS, DEFAULT_ASSISTANT_MAX_TOOL_ITERATIONS);
        return Math.min(Math.max(value, MIN_ASSISTANT_MAX_TOOL_ITERATIONS), MAX_ASSISTANT_MAX_TOOL_ITERATIONS);
    }

    public String getAssistantSystemPrompt() {
        String value = sysConfigService.getValue(ASSISTANT_SYSTEM_PROMPT);
        return StringUtils.hasText(value) ? value.trim() : DEFAULT_ASSISTANT_SYSTEM_PROMPT.trim();
    }

    public boolean isReadonlySqlEnabled() {
        return getBoolean(READONLY_SQL_ENABLED, DEFAULT_READONLY_SQL_ENABLED);
    }

    public String getReadonlySqlPermissionCode() {
        String value = sysConfigService.getValue(READONLY_SQL_PERMISSION_CODE);
        return StringUtils.hasText(value) ? value.trim() : DEFAULT_READONLY_SQL_PERMISSION_CODE;
    }

    public int getReadonlySqlMaxRows() {
        int value = getInt(READONLY_SQL_MAX_ROWS, DEFAULT_READONLY_SQL_MAX_ROWS);
        return Math.min(Math.max(value, 1), MAX_READONLY_SQL_ROWS);
    }

    public boolean isExecuteSqlEnabled() {
        return getBoolean(EXECUTE_SQL_ENABLED, DEFAULT_EXECUTE_SQL_ENABLED);
    }

    public String getExecuteSqlPermissionCode() {
        String value = sysConfigService.getValue(EXECUTE_SQL_PERMISSION_CODE);
        return StringUtils.hasText(value) ? value.trim() : DEFAULT_EXECUTE_SQL_PERMISSION_CODE;
    }

    public int getExecuteSqlMaxRows() {
        int value = getInt(EXECUTE_SQL_MAX_ROWS, DEFAULT_EXECUTE_SQL_MAX_ROWS);
        return Math.min(Math.max(value, 1), MAX_EXECUTE_SQL_ROWS);
    }

    public boolean isSchemaToolEnabled() {
        return getBoolean(SCHEMA_TOOL_ENABLED, DEFAULT_SCHEMA_TOOL_ENABLED);
    }

    public String getSchemaToolPermissionCode() {
        String value = sysConfigService.getValue(SCHEMA_TOOL_PERMISSION_CODE);
        return StringUtils.hasText(value) ? value.trim() : DEFAULT_SCHEMA_TOOL_PERMISSION_CODE;
    }

    /** 注入提示词的历史消息条数上限（约等于最近 N/2 轮对话）。 */
    public int getChatHistoryWindow() {
        int value = getInt(CHAT_HISTORY_WINDOW, DEFAULT_CHAT_HISTORY_WINDOW);
        return Math.min(Math.max(value, MIN_CHAT_HISTORY_WINDOW), MAX_CHAT_HISTORY_WINDOW);
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        String value = sysConfigService.getValue(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        return switch (value.trim().toLowerCase()) {
            case "1", "true", "yes", "on" -> true;
            case "0", "false", "no", "off" -> false;
            default -> defaultValue;
        };
    }

    private int getInt(String key, int defaultValue) {
        String value = sysConfigService.getValue(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
