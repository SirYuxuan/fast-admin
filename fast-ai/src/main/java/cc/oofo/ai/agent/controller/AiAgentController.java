package cc.oofo.ai.agent.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import cc.oofo.ai.agent.dto.AiChatMessageDto;
import cc.oofo.ai.agent.dto.AiChatRequest;
import cc.oofo.ai.agent.dto.AiChatSessionDto;
import cc.oofo.ai.agent.service.AiAgentChatService;
import cc.oofo.ai.agent.service.AiChatHistoryService;
import cc.oofo.framework.web.response.Rs;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/agent")
public class AiAgentController {

    private final AiAgentChatService chatService;
    private final AiChatHistoryService historyService;

    /**
     * AI 对话流式接口。前端通过 fetch POST 消费 text/event-stream。
     */
    @PostMapping(path = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody AiChatRequest request) {
        return chatService.chat(request);
    }

    /**
     * 当前用户的会话列表。
     */
    @GetMapping("/sessions")
    public Rs<List<AiChatSessionDto>> sessions() {
        String userId = StpUtil.getLoginIdAsString();
        List<AiChatSessionDto> list = historyService.listSessions(userId).stream()
                .map(s -> new AiChatSessionDto(s.getSessionId(), s.getTitle(),
                        s.getCreatedAt() == null ? null : s.getCreatedAt().toString(),
                        s.getUpdatedAt() == null ? null : s.getUpdatedAt().toString()))
                .toList();
        return Rs.ok(list);
    }

    /**
     * 指定会话的历史消息。
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Rs<List<AiChatMessageDto>> messages(@PathVariable String sessionId) {
        String userId = StpUtil.getLoginIdAsString();
        List<AiChatMessageDto> list = historyService.listMessages(sessionId, userId).stream()
                .map(m -> new AiChatMessageDto(m.getRole(), m.getContent(), m.getProcessJson(),
                        m.getModelName(), m.getModelProvider(), m.getModelCode(),
                        m.getCreatedAt() == null ? null : m.getCreatedAt().toString()))
                .toList();
        return Rs.ok(list);
    }

    /**
     * 删除会话及其消息。
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Rs<Void> deleteSession(@PathVariable String sessionId) {
        historyService.deleteSession(sessionId, StpUtil.getLoginIdAsString());
        return Rs.ok();
    }
}
