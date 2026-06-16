package cc.oofo.ai.agent.dto;

/**
 * AI 对话请求。
 *
 * @param sessionId 已有会话 ID，为空时后端创建临时会话
 * @param message   用户输入内容
 */
public record AiChatRequest(String sessionId, String message) {
}
