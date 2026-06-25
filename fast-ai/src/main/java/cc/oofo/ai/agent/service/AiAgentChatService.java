package cc.oofo.ai.agent.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import cc.oofo.ai.agent.dto.AiChatRequest;
import cc.oofo.ai.agent.dto.AiChatSseEvent;
import cc.oofo.ai.config.AiAssistantProperties;
import cc.oofo.ai.model.entity.AiModelConfig;
import cc.oofo.ai.model.service.AiModelConfigService;
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
    private final AiModelConfigService modelConfigService;
    private final SysUserApi sysUserApi;
    private final AsyncTaskExecutor applicationTaskExecutor;
    private final ObjectMapper objectMapper;

    private record ProcessItem(
            Long costMs,
            String detail,
            String id,
            String source,
            String status,
            String title,
            String toolName,
            String type) {
    }

    private record ModelInfo(String name, String provider, String code) {
    }

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
        List<ProcessItem> processItems = Collections.synchronizedList(new ArrayList<>());
        try {
            ModelInfo modelInfo = currentModelInfo();
            send(emitter, AiChatSseEvent.session(sessionId, modelInfo.name(), modelInfo.provider(), modelInfo.code()));

            if (!properties.isEnabled()) {
                send(emitter, AiChatSseEvent.error("AI 助手已关闭"));
                emitter.complete();
                return;
            }

            if (properties.isRequirePermission() && !permissionCodes.contains("ai:assistant:use")) {
                send(emitter, AiChatSseEvent.error("无权使用 AI 助手"));
                emitter.complete();
                return;
            }

            if (request == null || !StringUtils.hasText(request.message())) {
                send(emitter, AiChatSseEvent.error("请输入要发送的消息"));
                emitter.complete();
                return;
            }

            sendProcessEvent(emitter, processItems, AiChatSseEvent.thought("正在分析问题，并判断是否需要调用工具或 MCP。"));

            // 把 emitter 封装成 eventSink，异步线程安全地推送 tool 事件帧。
            ChatClient chatClient = chatClientFactory.create(
                    permissionCodes, sessionId, userId,
                    event -> sendProcessEventUnchecked(emitter, processItems, event));

            if (chatClient == null) {
                send(emitter, AiChatSseEvent.error("未配置可用的 AI 模型，请设置 AI_MODEL_CHAT 和对应 API Key"));
                emitter.complete();
                return;
            }

            // 先加载历史（仅含此前轮次）再落库本轮用户消息，避免历史与当前消息重复。
            List<Message> history = loadHistory(sessionId, userId, request.message());
            sendProcessEvent(emitter, processItems, AiChatSseEvent.thought("已加载上下文，正在生成回答。"));

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
                        persistAssistantMessage(sessionId, answer.toString(), toJson(processItems), modelInfo);
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
        emitter.send(SseEmitter.event().data(event));
    }

    private void sendUnchecked(SseEmitter emitter, AiChatSseEvent event) {
        try {
            send(emitter, event);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void sendProcessEvent(SseEmitter emitter, List<ProcessItem> processItems, AiChatSseEvent event)
            throws IOException {
        recordProcessEvent(processItems, event);
        send(emitter, event);
    }

    private void sendProcessEventUnchecked(SseEmitter emitter, List<ProcessItem> processItems, AiChatSseEvent event) {
        recordProcessEvent(processItems, event);
        sendUnchecked(emitter, event);
    }

    private void recordProcessEvent(List<ProcessItem> processItems, AiChatSseEvent event) {
        if (event == null) {
            return;
        }
        if ("thought".equals(event.type())) {
            processItems.add(new ProcessItem(null, null, UUID.randomUUID().toString(), null, "info",
                    StringUtils.hasText(event.text()) ? event.text() : "正在思考", null, "thought"));
            return;
        }
        if (!"tool".equals(event.type())) {
            return;
        }

        String source = StringUtils.hasText(event.source()) ? event.source() : "builtin";
        String toolName = StringUtils.hasText(event.toolName()) ? event.toolName() : "unknown_tool";
        String title = ("mcp".equals(source) ? "MCP" : "工具") + "调用：" + toolName;
        if ("start".equals(event.phase())) {
            processItems.add(new ProcessItem(null, event.args(), UUID.randomUUID().toString(), source, "running",
                    title, toolName, "tool"));
            return;
        }

        String status = Boolean.TRUE.equals(event.ok()) ? "success" : "error";
        synchronized (processItems) {
            for (int i = processItems.size() - 1; i >= 0; i--) {
                ProcessItem item = processItems.get(i);
                if ("tool".equals(item.type())
                        && toolName.equals(item.toolName())
                        && source.equals(item.source())
                        && "running".equals(item.status())) {
                    processItems.set(i, new ProcessItem(event.costMs(), item.detail(), item.id(), source, status,
                            item.title(), toolName, "tool"));
                    return;
                }
            }
        }
        processItems.add(new ProcessItem(event.costMs(), null, UUID.randomUUID().toString(), source, status,
                title, toolName, "tool"));
    }

    private String toJson(List<ProcessItem> processItems) {
        if (processItems.isEmpty()) {
            return null;
        }
        try {
            synchronized (processItems) {
                return objectMapper.writeValueAsString(new ArrayList<>(processItems));
            }
        } catch (Exception e) {
            log.warn("Serialize AI process events failed", e);
            return null;
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

    private void persistAssistantMessage(String sessionId, String content, String processJson, ModelInfo modelInfo) {
        try {
            historyService.saveAssistantMessage(sessionId, content, processJson,
                    modelInfo.name(), modelInfo.provider(), modelInfo.code());
        } catch (Exception e) {
            log.warn("Persist assistant message failed", e);
        }
    }

    private ModelInfo currentModelInfo() {
        try {
            AiModelConfig activeModel = modelConfigService.getActiveEnabled();
            if (activeModel != null) {
                return new ModelInfo(activeModel.getName(), activeModel.getProvider(), activeModel.getModel());
            }
        } catch (Exception e) {
            log.debug("Read current AI model failed", e);
        }
        return new ModelInfo(null, null, null);
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
