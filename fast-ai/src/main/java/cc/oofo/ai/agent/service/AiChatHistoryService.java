package cc.oofo.ai.agent.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import cc.oofo.ai.agent.entity.AiChatMessage;
import cc.oofo.ai.agent.entity.AiChatSession;
import cc.oofo.ai.agent.mapper.AiChatMessageMapper;
import cc.oofo.ai.agent.mapper.AiChatSessionMapper;
import lombok.RequiredArgsConstructor;

/**
 * AI 会话与消息持久化：负责会话落库、历史加载，为多轮对话提供记忆。
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AiChatHistoryService {

    /** 注入提示词的历史消息条数上限（约等于最近 N/2 轮对话）。 */
    private static final int HISTORY_LIMIT = 20;
    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";
    private static final int TITLE_MAX = 50;

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;

    /**
     * 确保会话存在，首条消息时创建并以其作为标题。
     */
    public void ensureSession(String sessionId, String userId, String firstMessage) {
        if (!StringUtils.hasText(sessionId) || getSession(sessionId) != null) {
            return;
        }
        AiChatSession session = new AiChatSession();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setTitle(buildTitle(firstMessage));
        sessionMapper.insert(session);
    }

    /**
     * 加载会话最近若干条历史消息，按时间正序返回为 Spring AI Message。
     */
    @Transactional(readOnly = true)
    public List<Message> loadHistory(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return List.of();
        }
        List<AiChatMessage> recent = messageMapper.selectList(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, sessionId)
                .orderByDesc(AiChatMessage::getCreatedAt)
                .last("limit " + HISTORY_LIMIT));
        Collections.reverse(recent);

        List<Message> messages = new ArrayList<>(recent.size());
        for (AiChatMessage item : recent) {
            if (!StringUtils.hasText(item.getContent())) {
                continue;
            }
            messages.add(ROLE_ASSISTANT.equals(item.getRole())
                    ? new AssistantMessage(item.getContent())
                    : new UserMessage(item.getContent()));
        }
        return messages;
    }

    public void saveMessage(String sessionId, String role, String content) {
        if (!StringUtils.hasText(sessionId) || !StringUtils.hasText(content)) {
            return;
        }
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        messageMapper.insert(message);
    }

    public void saveUserMessage(String sessionId, String content) {
        saveMessage(sessionId, ROLE_USER, content);
    }

    public void saveAssistantMessage(String sessionId, String content) {
        saveMessage(sessionId, ROLE_ASSISTANT, content);
    }

    @Transactional(readOnly = true)
    public List<AiChatSession> listSessions(String userId) {
        return sessionMapper.selectList(new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getUserId, userId)
                .orderByDesc(AiChatSession::getUpdatedAt));
    }

    @Transactional(readOnly = true)
    public List<AiChatMessage> listMessages(String sessionId, String userId) {
        AiChatSession session = getSession(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            return List.of();
        }
        return messageMapper.selectList(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, sessionId)
                .orderByAsc(AiChatMessage::getCreatedAt));
    }

    public void deleteSession(String sessionId, String userId) {
        AiChatSession session = getSession(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            return;
        }
        sessionMapper.deleteById(session.getId());
        messageMapper.delete(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, sessionId));
    }

    private AiChatSession getSession(String sessionId) {
        return sessionMapper.selectOne(new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getSessionId, sessionId)
                .last("limit 1"));
    }

    private String buildTitle(String firstMessage) {
        if (!StringUtils.hasText(firstMessage)) {
            return "新会话";
        }
        String trimmed = firstMessage.strip();
        return trimmed.length() > TITLE_MAX ? trimmed.substring(0, TITLE_MAX) : trimmed;
    }
}
