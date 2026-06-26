package cc.oofo.ai.agent.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 会话消息。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_chat_message")
public class AiChatMessage extends BaseEntity<AiChatMessage> {

    /** 所属会话业务 ID。 */
    private String sessionId;

    /** 角色：user / assistant。 */
    private String role;

    /** 消息内容。 */
    private String content;

    /** 助手消息的思考与工具过程 JSON。 */
    private String processJson;

    /** 助手消息使用的模型配置名称。 */
    private String modelName;

    /** 助手消息使用的模型提供方。 */
    private String modelProvider;

    /** 助手消息使用的模型编码。 */
    private String modelCode;

    /** 本轮输入（提示）token 数，助手消息记录。 */
    private Integer promptTokens;

    /** 本轮输出（补全）token 数，助手消息记录。 */
    private Integer completionTokens;

    /** 本轮总 token 数，助手消息记录。 */
    private Integer totalTokens;
}
