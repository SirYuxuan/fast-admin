package cc.oofo.ai.agent.dto;

/**
 * 会话消息项。
 */
public record AiChatMessageDto(String role, String content, String createdAt) {
}
