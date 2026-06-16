package cc.oofo.ai.model.dto;

import lombok.Data;

/**
 * AI 模型连通性测试结果。
 */
@Data
public class AiModelTestResultDto {

    /** 测试是否成功。 */
    private boolean success;

    /** 本次测试延时（毫秒）。 */
    private long latencyMs;

    /** 失败时的错误信息。 */
    private String message;
}
