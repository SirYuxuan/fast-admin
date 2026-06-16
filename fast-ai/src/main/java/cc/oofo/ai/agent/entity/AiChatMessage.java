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
}
