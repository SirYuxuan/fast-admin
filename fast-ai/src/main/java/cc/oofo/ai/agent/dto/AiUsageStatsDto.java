package cc.oofo.ai.agent.dto;

import java.util.List;

/**
 * AI 用量统计结果。
 *
 * @param totalMessages    助手消息总条数
 * @param promptTokens     输入 token 累计
 * @param completionTokens 输出 token 累计
 * @param totalTokens      总 token 累计
 * @param byModel          按模型分组的用量
 * @param byDay            最近若干天的每日用量
 */
public record AiUsageStatsDto(
        long totalMessages,
        long promptTokens,
        long completionTokens,
        long totalTokens,
        List<ModelUsage> byModel,
        List<DailyUsage> byDay) {

    public record ModelUsage(String modelCode, String modelName, long messages, long totalTokens) {
    }

    public record DailyUsage(String day, long messages, long totalTokens) {
    }
}
