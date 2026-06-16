package cc.oofo.ai.agent.dto;

/**
 * 前端消费的 SSE 事件。
 *
 * <p>v1 先只输出会话、文本增量、完成和错误事件；工具调用事件后续接入 tools 后再扩展。</p>
 */
public record AiChatSseEvent(
        String type,
        String sessionId,
        String text,
        String message,
        String messageId) {

    public static AiChatSseEvent session(String sessionId) {
        return new AiChatSseEvent("session", sessionId, null, null, null);
    }

    public static AiChatSseEvent delta(String text) {
        return new AiChatSseEvent("delta", null, text, null, null);
    }

    public static AiChatSseEvent done(String messageId) {
        return new AiChatSseEvent("done", null, null, null, messageId);
    }

    public static AiChatSseEvent error(String message) {
        return new AiChatSseEvent("error", null, null, message, null);
    }
}
