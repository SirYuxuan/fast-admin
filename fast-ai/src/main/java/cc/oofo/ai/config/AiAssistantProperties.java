package cc.oofo.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * AI 助手配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai-assistant")
public class AiAssistantProperties {

    private boolean enabled = true;

    private boolean requirePermission = false;

    /** 单轮对话最大工具调用轮次，防止工具调用失控。 */
    private int maxToolIterations = 8;

    /**
     * v1 的基础系统提示词。后续可以迁移到 sys_config 或独立 ai_setting。
     */
    private String systemPrompt = """
            你是 Fast Admin 后台的 AI 运维助手。
            回答要简洁、准确；当你无法确认后台事实时，明确说明需要工具或数据支持。
            当前版本仅支持对话，不得声称已经执行后台写操作。
            """;
}
