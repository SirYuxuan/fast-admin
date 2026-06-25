package cc.oofo.ai.agent.dto;

import java.util.List;

/**
 * AI 对话请求。
 *
 * @param sessionId    已有会话 ID，为空时后端创建临时会话
 * @param message      用户输入内容
 * @param toolMode     内置工具挂载模式：auto/off/manual，空值按 auto 处理
 * @param toolCodes    manual 模式下允许挂载的内置工具编码
 * @param mcpMode      MCP 挂载模式：auto/off/manual，空值按 off 处理
 * @param mcpServerIds manual 模式下允许挂载的 MCP 服务 ID
 */
public record AiChatRequest(
        String sessionId,
        String message,
        String toolMode,
        List<String> toolCodes,
        String mcpMode,
        List<String> mcpServerIds) {
}
