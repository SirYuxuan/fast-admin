package cc.oofo.ai.agent.dto;

/**
 * 前端消费的 SSE 事件。
 *
 * <p>type 枚举：session / thought / delta / tool / done / error</p>
 */
public record AiChatSseEvent(
        String type,
        String sessionId,
        String modelName,
        String modelProvider,
        String modelCode,
        String text,
        String message,
        String messageId,
        // tool 事件专用字段
        String toolName,
        String source,
        String phase,
        String args,
        Boolean ok,
        Long costMs) {

    public static AiChatSseEvent session(String sessionId) {
        return session(sessionId, null, null, null);
    }

    public static AiChatSseEvent session(String sessionId, String modelName, String modelProvider, String modelCode) {
        return new AiChatSseEvent("session", sessionId, modelName, modelProvider, modelCode, null, null, null, null,
                null, null, null, null, null);
    }

    public static AiChatSseEvent thought(String text) {
        return new AiChatSseEvent("thought", null, null, null, null, text, null, null, null, null, null, null, null,
                null);
    }

    public static AiChatSseEvent delta(String text) {
        return new AiChatSseEvent("delta", null, null, null, null, text, null, null, null, null, null, null, null,
                null);
    }

    public static AiChatSseEvent done(String messageId) {
        return new AiChatSseEvent("done", null, null, null, null, null, null, messageId, null, null, null, null, null,
                null);
    }

    public static AiChatSseEvent error(String message) {
        return new AiChatSseEvent("error", null, null, null, null, null, message, null, null, null, null, null, null,
                null);
    }

    /** 工具执行前等待用户确认的事件，sql 为待执行语句，confirmToken 用于前端回调确认接口。 */
    public static AiChatSseEvent toolPending(String toolName, String sql, String confirmToken) {
        return new AiChatSseEvent("tool", null, null, null, null, null, null, confirmToken,
                toolName, "builtin", "pending", sql, null, null);
    }

    /** 工具调用开始事件，args 为入参 JSON 字符串。 */
    public static AiChatSseEvent toolStart(String toolName, String args) {
        return toolStart(toolName, "builtin", args);
    }

    public static AiChatSseEvent toolStart(String toolName, String source, String args) {
        return new AiChatSseEvent("tool", null, null, null, null, null, null, null, toolName, source, "start", args,
                null, null);
    }

    /** 工具调用结束事件，ok 表示是否成功。 */
    public static AiChatSseEvent toolEnd(String toolName, boolean ok) {
        return toolEnd(toolName, "builtin", ok, null);
    }

    public static AiChatSseEvent toolEnd(String toolName, String source, boolean ok, Long costMs) {
        return new AiChatSseEvent("tool", null, null, null, null, null, null, null, toolName, source, "end", null, ok,
                costMs);
    }
}
