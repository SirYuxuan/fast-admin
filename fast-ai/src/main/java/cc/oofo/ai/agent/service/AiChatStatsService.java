package cc.oofo.ai.agent.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cc.oofo.ai.agent.dto.AiUsageStatsDto;
import cc.oofo.ai.agent.entity.AiChatMessage;
import cc.oofo.ai.agent.mapper.AiChatMessageMapper;
import lombok.RequiredArgsConstructor;

/**
 * AI 对话用量统计：基于 ai_chat_message 的助手消息 token 聚合。
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AiChatStatsService {

    private static final int MAX_DAYS = 90;

    private final AiChatMessageMapper messageMapper;

    public AiUsageStatsDto stats(int days) {
        int window = Math.min(Math.max(days, 1), MAX_DAYS);

        Map<String, Object> totals = totals();
        List<AiUsageStatsDto.ModelUsage> byModel = byModel();
        List<AiUsageStatsDto.DailyUsage> byDay = byDay(window);

        return new AiUsageStatsDto(
                toLong(totals.get("messages")),
                toLong(totals.get("promptTokens")),
                toLong(totals.get("completionTokens")),
                toLong(totals.get("totalTokens")),
                byModel,
                byDay);
    }

    private Map<String, Object> totals() {
        QueryWrapper<AiChatMessage> qw = assistantBase()
                .select("count(*) as messages",
                        "ifnull(sum(prompt_tokens),0) as promptTokens",
                        "ifnull(sum(completion_tokens),0) as completionTokens",
                        "ifnull(sum(total_tokens),0) as totalTokens");
        List<Map<String, Object>> rows = messageMapper.selectMaps(qw);
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    private List<AiUsageStatsDto.ModelUsage> byModel() {
        QueryWrapper<AiChatMessage> qw = assistantBase()
                .isNotNull("model_code")
                .select("model_code as modelCode",
                        "max(model_name) as modelName",
                        "count(*) as messages",
                        "ifnull(sum(total_tokens),0) as totalTokens")
                .groupBy("model_code")
                .orderByDesc("totalTokens");
        List<AiUsageStatsDto.ModelUsage> result = new ArrayList<>();
        for (Map<String, Object> row : messageMapper.selectMaps(qw)) {
            result.add(new AiUsageStatsDto.ModelUsage(
                    toStr(row.get("modelCode")),
                    toStr(row.get("modelName")),
                    toLong(row.get("messages")),
                    toLong(row.get("totalTokens"))));
        }
        return result;
    }

    private List<AiUsageStatsDto.DailyUsage> byDay(int days) {
        QueryWrapper<AiChatMessage> qw = assistantBase()
                .ge("created_at", LocalDate.now().minusDays(days - 1L).atStartOfDay())
                .select("date(created_at) as day",
                        "count(*) as messages",
                        "ifnull(sum(total_tokens),0) as totalTokens")
                .groupBy("date(created_at)")
                .orderByAsc("day");
        List<AiUsageStatsDto.DailyUsage> result = new ArrayList<>();
        for (Map<String, Object> row : messageMapper.selectMaps(qw)) {
            result.add(new AiUsageStatsDto.DailyUsage(
                    toStr(row.get("day")),
                    toLong(row.get("messages")),
                    toLong(row.get("totalTokens"))));
        }
        return result;
    }

    private QueryWrapper<AiChatMessage> assistantBase() {
        return new QueryWrapper<AiChatMessage>().eq("role", "assistant");
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String toStr(Object value) {
        return value == null ? null : value.toString();
    }
}
