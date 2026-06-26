package cc.oofo.ai.agent.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cc.oofo.ai.agent.entity.AiToolCallLog;
import cc.oofo.ai.agent.mapper.AiToolCallLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 工具调用审计日志写入器，供内置工具与 MCP 工具两条执行路径共用。
 * 写入失败不影响主流程。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiToolAuditLogger {

    public static final String SOURCE_BUILTIN = "builtin";
    public static final String SOURCE_MCP = "mcp";

    private static final int RESULT_MAX_CHARS = 4000;

    private final AiToolCallLogMapper toolCallLogMapper;

    public void write(String sessionId, String operatorId, String toolName, String source,
            String argsJson, String resultJson, boolean success, String errorMsg, long costMs) {
        try {
            AiToolCallLog entity = new AiToolCallLog();
            entity.setSessionId(sessionId);
            entity.setOperatorId(operatorId);
            entity.setToolName(toolName);
            entity.setSource(source);
            entity.setArgumentsJson(truncate(argsJson));
            entity.setResultJson(truncate(resultJson));
            entity.setSuccess(success);
            entity.setErrorMsg(StringUtils.hasText(errorMsg) ? truncate(errorMsg) : null);
            entity.setCostMs(costMs);
            toolCallLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("Failed to write tool call audit log for '{}': {}", toolName, e.getMessage());
        }
    }

    public String truncate(String value) {
        if (value == null || value.length() <= RESULT_MAX_CHARS) {
            return value;
        }
        return value.substring(0, RESULT_MAX_CHARS);
    }
}
