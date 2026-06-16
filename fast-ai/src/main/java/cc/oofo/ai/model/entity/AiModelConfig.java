package cc.oofo.ai.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 模型配置。
 *
 * <p>用于后台维护可选模型，当前 Agent 仍先走 Spring AI 自动配置，后续可从这里读取激活模型。</p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_model_config")
public class AiModelConfig extends BaseEntity<AiModelConfig> {

    /** 配置名称，方便运维识别。 */
    private String name;

    /** 模型提供方：anthropic / openai / openai-compatible。 */
    private String provider;

    /** 模型名称，例如 claude-opus-4-8、gpt-5-mini、deepseek-chat。 */
    private String model;

    /** OpenAI 兼容接口地址；Anthropic 可为空。 */
    private String baseUrl;

    /** API Key，返回前端时会脱敏。 */
    private String apiKey;

    /** 是否启用该配置。 */
    private Boolean enabled;

    /** 是否为当前默认模型配置。 */
    private Boolean active;

    /** 采样温度，空值表示使用模型默认值。 */
    private Double temperature;

    /** 最大输出 token，空值表示使用模型默认值。 */
    private Integer maxTokens;

    /** 备注。 */
    private String remark;

    /** 上次测试延时（毫秒），空值表示尚未测试。 */
    private Long lastLatencyMs;

    /** 上次测试是否成功。 */
    private Boolean lastTestOk;

    /** 上次测试时间。 */
    private LocalDateTime lastTestedAt;
}
