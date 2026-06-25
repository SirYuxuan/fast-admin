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

    public static final String READONLY_SQL_ENABLED = "ai.readonly-sql.enabled";
    public static final String READONLY_SQL_PERMISSION_CODE = "ai.readonly-sql.permission-code";
    public static final String READONLY_SQL_MAX_ROWS = "ai.readonly-sql.max-rows";

    public static final String EXECUTE_SQL_ENABLED = "ai.execute-sql.enabled";
    public static final String EXECUTE_SQL_PERMISSION_CODE = "ai.execute-sql.permission-code";
    public static final String EXECUTE_SQL_MAX_ROWS = "ai.execute-sql.max-rows";

    private static final boolean DEFAULT_READONLY_SQL_ENABLED = true;
    private static final String DEFAULT_READONLY_SQL_PERMISSION_CODE = "ai:sql:readonly";
    private static final int DEFAULT_READONLY_SQL_MAX_ROWS = 100;
    private static final int MAX_READONLY_SQL_ROWS = 100;

    private static final boolean DEFAULT_EXECUTE_SQL_ENABLED = false;
    private static final String DEFAULT_EXECUTE_SQL_PERMISSION_CODE = "ai:sql:execute";
    private static final int DEFAULT_EXECUTE_SQL_MAX_ROWS = 100;
    private static final int MAX_EXECUTE_SQL_ROWS = 500;

    private final SysConfigService sysConfigService;

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
