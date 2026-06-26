package cc.oofo.ai.agent.dto;

/**
 * 会话消息项。
 */
public record AiChatMessageDto(
        String role,
        String content,
        String processJson,
        String modelName,
        String modelProvider,
        String modelCode,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        String createdAt) {
}
