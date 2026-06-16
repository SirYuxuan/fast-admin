package cc.oofo.ai.agent.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import cc.oofo.ai.agent.dto.AiChatRequest;
import cc.oofo.ai.agent.dto.AiChatSseEvent;
import cc.oofo.ai.config.AiAssistantProperties;
import cc.oofo.system.user.api.SysUserApi;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAgentChatService {

    private final AiAssistantProperties properties;
    private final AiChatClientFactory chatClientFactory;
    private final AiChatHistoryService historyService;
    private final SysUserApi sysUserApi;
    private final AsyncTaskExecutor applicationTaskExecutor;

    /**
     * 创建 SSE 流。Servlet 请求线程只负责返回 emitter，模型调用放到异步线程里执行。
     */
    public SseEmitter chat(AiChatRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        String sessionId = request != null && StringUtils.hasText(request.sessionId())
                ? request.sessionId()
                : UUID.randomUUID().toString();
        List<String> permissionCodes = currentPermissionCodes();
        String userId = currentUserId();

        applicationTaskExecutor.execute(() -> doChat(request, sessionId, userId, permissionCodes, emitter));
        return emitter;
    }

    /**
     * 执行一次模型对话，并将模型 delta 逐帧转发给前端。
     */
    private void doChat(AiChatRequest request, String sessionId, String userId, List<String> permissionCodes,
            SseEmitter emitter) {
        try {
            send(emitter, AiChatSseEvent.session(sessionId));

            if (!properties.isEnabled()) {
                send(emitter, AiChatSseEvent.error("AI 助手已关闭"));
                emitter.complete();
                return;
            }
            if (request == null || !StringUtils.hasText(request.message())) {
                send(emitter, AiChatSseEvent.error("请输入要发送的消息"));
                emitter.complete();
                return;
            }

            // 数据库当前模型优先；没有当前模型时回退到 Spring AI 自动配置。
            ChatClient chatClient = chatClientFactory.create(permissionCodes);
            if (chatClient == null) {
                send(emitter, AiChatSseEvent.error("未配置可用的 AI 模型，请设置 AI_MODEL_CHAT 和对应 API Key"));
                emitter.complete();
                return;
            }

            // 先加载历史（仅含此前轮次）再落库本轮用户消息，避免历史与当前消息重复。
            List<Message> history = loadHistory(sessionId, userId, request.message());

            StringBuilder answer = new StringBuilder();
            chatClient.prompt()
                    .system(properties.getSystemPrompt())
                    .messages(history)
                    .user(request.message())
                    .stream()
                    .content()
                    .doOnNext(delta -> {
                        answer.append(delta);
                        sendUnchecked(emitter, AiChatSseEvent.delta(delta));
                    })
                    .doOnError(error -> {
                        log.warn("AI chat stream failed", error);
                        sendUnchecked(emitter, AiChatSseEvent.error("AI 响应失败：" + error.getMessage()));
                        emitter.complete();
                    })
                    .doOnComplete(() -> {
                        persistAssistantMessage(sessionId, answer.toString());
                        sendUnchecked(emitter, AiChatSseEvent.done(UUID.randomUUID().toString()));
                        emitter.complete();
                    })
                    .subscribe();
        } catch (Exception e) {
            log.warn("AI chat request failed", e);
            sendUnchecked(emitter, AiChatSseEvent.error("AI 请求失败：" + e.getMessage()));
            emitter.complete();
        }
    }

    private void send(SseEmitter emitter, AiChatSseEvent event) throws IOException {
        // 前端只依赖 data 中的 type 字段，保持 SSE 事件名统一可以简化解析逻辑。
        emitter.send(SseEmitter.event().data(event));
    }

    private void sendUnchecked(SseEmitter emitter, AiChatSseEvent event) {
        try {
            send(emitter, event);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 确保会话存在、落库本轮用户消息，并返回此前轮次的历史消息。持久化失败不影响对话。
     */
    private List<Message> loadHistory(String sessionId, String userId, String message) {
        try {
            historyService.ensureSession(sessionId, userId, message);
            List<Message> history = historyService.loadHistory(sessionId);
            historyService.saveUserMessage(sessionId, message);
            return history;
        } catch (Exception e) {
            log.warn("Persist chat session/user message failed", e);
            return List.of();
        }
    }

    private void persistAssistantMessage(String sessionId, String content) {
        try {
            historyService.saveAssistantMessage(sessionId, content);
        } catch (Exception e) {
            log.warn("Persist assistant message failed", e);
        }
    }

    private String currentUserId() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsString();
            }
        } catch (Exception e) {
            log.debug("Read current user id failed", e);
        }
        return null;
    }

    private List<String> currentPermissionCodes() {
        try {
            if (StpUtil.isLogin()) {
                return sysUserApi.getUserPermissionCodes(StpUtil.getLoginIdAsString());
            }
        } catch (Exception e) {
            log.debug("Read current permission codes failed", e);
        }
        return List.of();
    }
}
