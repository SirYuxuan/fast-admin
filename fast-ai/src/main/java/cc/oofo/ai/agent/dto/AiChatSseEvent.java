package cc.oofo.ai.agent.dto;

/**
 * 前端消费的 SSE 事件。
 *
 * <p>type 枚举：session / delta / tool / done / error</p>
 */
public record AiChatSseEvent(
        String type,
        String sessionId,
        String text,
        String message,
        String messageId,
        // tool 事件专用字段
        String toolName,
        String phase,
        String args,
        Boolean ok) {

    public static AiChatSseEvent session(String sessionId) {
        return new AiChatSseEvent("session", sessionId, null, null, null, null, null, null, null);
    }

    public static AiChatSseEvent delta(String text) {
        return new AiChatSseEvent("delta", null, text, null, null, null, null, null, null);
    }

    public static AiChatSseEvent done(String messageId) {
        return new AiChatSseEvent("done", null, null, null, messageId, null, null, null, null);
    }

    public static AiChatSseEvent error(String message) {
        return new AiChatSseEvent("error", null, null, message, null, null, null, null, null);
    }

    /** 工具调用开始事件，args 为入参 JSON 字符串。 */
    public static AiChatSseEvent toolStart(String toolName, String args) {
        return new AiChatSseEvent("tool", null, null, null, null, toolName, "start", args, null);
    }

    /** 工具调用结束事件，ok 表示是否成功。 */
    public static AiChatSseEvent toolEnd(String toolName, boolean ok) {
        return new AiChatSseEvent("tool", null, null, null, null, toolName, "end", null, ok);
    }
}
