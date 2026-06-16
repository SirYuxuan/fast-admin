package cc.oofo.ai.agent.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 工具调用审计日志。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_tool_call_log")
public class AiToolCallLog extends BaseEntity<AiToolCallLog> {

    /** 对话会话业务 ID。 */
    private String sessionId;

    /** 操作人用户 ID。 */
    private String operatorId;

    /** 工具编码，与 ai_tool_config.tool_code 或 MCP 工具名对应。 */
    private String toolName;

    /** 工具来源：builtin（数据库配置工具）/ mcp（外部 MCP 工具）。 */
    private String source;

    /** 调用入参 JSON。 */
    private String argumentsJson;

    /** 执行结果（超长时截断至 4000 字符）。 */
    private String resultJson;

    /** 是否执行成功。 */
    private Boolean success;

    /** 失败时的错误信息。 */
    private String errorMsg;

    /** 执行耗时（毫秒）。 */
    private Long costMs;
}
