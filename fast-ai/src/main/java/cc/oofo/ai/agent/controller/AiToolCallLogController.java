package cc.oofo.ai.agent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.ai.agent.entity.AiToolCallLog;
import cc.oofo.ai.agent.entity.query.AiToolCallLogQuery;
import cc.oofo.ai.agent.service.AiToolCallLogService;
import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import lombok.RequiredArgsConstructor;

/**
 * AI 工具调用审计日志查询接口（只读）。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/tool-log")
public class AiToolCallLogController {

    private final AiToolCallLogService service;

    @GetMapping
    public Ps<AiToolCallLog> page(AiToolCallLogQuery query) {
        return Ps.ok(service.page(query));
    }

    @GetMapping("/{id}")
    public Rs<AiToolCallLog> detail(@PathVariable String id) {
        return Rs.ok(service.getById(id));
    }
}
