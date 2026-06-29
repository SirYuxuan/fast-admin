package cc.oofo.ai.config;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import cc.oofo.ai.mcp.service.AiMcpClientManager;
import cc.oofo.ai.mcp.service.AiMcpSettingService;
import cc.oofo.ai.rag.service.AiRagConfigService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.config.entity.SysConfig;
import cc.oofo.system.config.service.SysConfigService;
import lombok.RequiredArgsConstructor;

/**
 * AI 运维配置聚合服务。
 */
@Service
@RequiredArgsConstructor
public class AiConfigService {

    private final AiAssistantSettingService assistantSettingService;
    private final AiMcpSettingService mcpSettingService;
    private final AiMcpClientManager mcpClientManager;
    private final AiRagConfigService ragConfigService;
    private final SysConfigService sysConfigService;

    public AiConfigDto current() {
        AiConfigDto dto = new AiConfigDto();
        dto.setAssistantEnabled(assistantSettingService.isAssistantEnabled());
        dto.setAssistantRequirePermission(assistantSettingService.isAssistantRequirePermission());
        dto.setAssistantMaxToolIterations(assistantSettingService.getAssistantMaxToolIterations());
        dto.setAssistantSystemPrompt(assistantSettingService.getAssistantSystemPrompt());
        dto.setMcpClientEnabled(mcpSettingService.isClientEnabled());
        dto.setChatHistoryWindow(assistantSettingService.getChatHistoryWindow());
        dto.setReadonlySqlEnabled(assistantSettingService.isReadonlySqlEnabled());
        dto.setReadonlySqlPermissionCode(assistantSettingService.getReadonlySqlPermissionCode());
        dto.setReadonlySqlMaxRows(assistantSettingService.getReadonlySqlMaxRows());
        dto.setExecuteSqlEnabled(assistantSettingService.isExecuteSqlEnabled());
        dto.setExecuteSqlPermissionCode(assistantSettingService.getExecuteSqlPermissionCode());
        dto.setExecuteSqlMaxRows(assistantSettingService.getExecuteSqlMaxRows());
        dto.setSchemaToolEnabled(assistantSettingService.isSchemaToolEnabled());
        dto.setSchemaToolPermissionCode(assistantSettingService.getSchemaToolPermissionCode());
        dto.setRag(ragConfigService.current());
        return dto;
    }

    @Transactional
    public void save(AiConfigDto dto) {
        if (dto == null) {
            throw new BizException("AI 配置不能为空");
        }

        boolean oldMcpClientEnabled = mcpSettingService.isClientEnabled();

        upsert(AiAssistantSettingService.ASSISTANT_ENABLED, "AI助手开关",
                String.valueOf(!Boolean.FALSE.equals(dto.getAssistantEnabled())),
                "控制 AI 运维助手是否启用");
        upsert(AiAssistantSettingService.ASSISTANT_REQUIRE_PERMISSION, "AI助手使用权限校验",
                String.valueOf(Boolean.TRUE.equals(dto.getAssistantRequirePermission())),
                "控制使用 AI 运维助手时是否校验 ai:assistant:use 权限码");
        upsert(AiAssistantSettingService.ASSISTANT_MAX_TOOL_ITERATIONS, "AI助手最大工具轮次",
                String.valueOf(clamp(dto.getAssistantMaxToolIterations(), 1, 20, 8)),
                "单轮对话最大工具调用轮次，防止工具调用失控");
        upsert(AiAssistantSettingService.ASSISTANT_SYSTEM_PROMPT, "AI助手系统提示词",
                trimToDefault(dto.getAssistantSystemPrompt(), assistantSettingService.getAssistantSystemPrompt()),
                "AI 运维助手的基础系统提示词");
        upsert(AiMcpSettingService.MCP_CLIENT_ENABLED, "AI MCP 客户端开关",
                String.valueOf(!Boolean.FALSE.equals(dto.getMcpClientEnabled())),
                "控制 AI 对话是否允许加载已启用的 MCP 服务");
        upsert(AiAssistantSettingService.CHAT_HISTORY_WINDOW, "AI对话历史窗口",
                String.valueOf(clamp(dto.getChatHistoryWindow(), 2, 100, 20)),
                "每轮对话注入提示词的历史消息条数上限（2~100）");
        upsert(AiAssistantSettingService.READONLY_SQL_ENABLED, "AI只读SQL工具开关",
                String.valueOf(!Boolean.FALSE.equals(dto.getReadonlySqlEnabled())),
                "控制内置 execute_readonly_sql 工具是否注册给模型");
        upsert(AiAssistantSettingService.READONLY_SQL_PERMISSION_CODE, "AI只读SQL工具权限码",
                trimToEmpty(dto.getReadonlySqlPermissionCode()),
                "调用内置 execute_readonly_sql 工具需要的权限码，留空则不校验");
        upsert(AiAssistantSettingService.READONLY_SQL_MAX_ROWS, "AI只读SQL最大返回行数",
                String.valueOf(clamp(dto.getReadonlySqlMaxRows(), 1, 100, 100)),
                "内置 execute_readonly_sql 工具单次最多返回行数，代码层最大 100");
        upsert(AiAssistantSettingService.EXECUTE_SQL_ENABLED, "AI执行SQL工具开关",
                String.valueOf(Boolean.TRUE.equals(dto.getExecuteSqlEnabled())),
                "控制内置 execute_sql 工具是否注册给模型");
        upsert(AiAssistantSettingService.EXECUTE_SQL_PERMISSION_CODE, "AI执行SQL工具权限码",
                trimToEmpty(dto.getExecuteSqlPermissionCode()),
                "调用内置 execute_sql 工具需要的权限码，留空则不校验权限");
        upsert(AiAssistantSettingService.EXECUTE_SQL_MAX_ROWS, "AI执行SQL最大返回行数",
                String.valueOf(clamp(dto.getExecuteSqlMaxRows(), 1, 500, 100)),
                "内置 execute_sql 工具查询语句单次最多返回行数，代码层最大 500");
        upsert(AiAssistantSettingService.SCHEMA_TOOL_ENABLED, "AI表结构工具开关",
                String.valueOf(!Boolean.FALSE.equals(dto.getSchemaToolEnabled())),
                "控制内置 describe_schema 工具是否注册给模型");
        upsert(AiAssistantSettingService.SCHEMA_TOOL_PERMISSION_CODE, "AI表结构工具权限码",
                trimToEmpty(dto.getSchemaToolPermissionCode()),
                "调用内置 describe_schema 工具需要的权限码，留空则不校验");

        ragConfigService.save(dto.getRag());

        if (oldMcpClientEnabled != mcpSettingService.isClientEnabled()) {
            mcpClientManager.reload();
        }
    }

    private void upsert(String key, String name, String value, String remark) {
        SysConfig config = sysConfigService.query().eq("config_key", key).one();
        if (config == null) {
            config = new SysConfig();
            config.setId(key.replace('.', '_'));
            config.setConfigKey(key);
            config.setConfigName(name);
            config.setConfigType(1);
            config.setConfigValue(value);
            config.setRemark(remark);
            sysConfigService.save(config);
            return;
        }
        config.setConfigName(name);
        config.setConfigValue(value);
        config.setConfigType(1);
        config.setRemark(remark);
        sysConfigService.updateById(config);
    }

    private int clamp(Integer value, int min, int max, int defaultValue) {
        int number = value == null ? defaultValue : value;
        return Math.min(Math.max(number, min), max);
    }

    private String trimToEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String trimToDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }
}
