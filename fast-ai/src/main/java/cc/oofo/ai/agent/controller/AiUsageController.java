package cc.oofo.ai.agent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.ai.agent.dto.AiUsageStatsDto;
import cc.oofo.ai.agent.service.AiChatStatsService;
import cc.oofo.framework.web.response.Rs;
import lombok.RequiredArgsConstructor;

/**
 * AI 用量统计接口（只读）。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/usage")
public class AiUsageController {

    private final AiChatStatsService statsService;

    @GetMapping("/stats")
    public Rs<AiUsageStatsDto> stats(@RequestParam(defaultValue = "14") int days) {
        return Rs.ok(statsService.stats(days));
    }
}
