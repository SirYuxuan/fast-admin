package cc.oofo.ai.agent.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 会话。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_chat_session")
public class AiChatSession extends BaseEntity<AiChatSession> {

    /** 会话业务 ID，前端生成并在多轮间复用。 */
    private String sessionId;

    /** 所属用户 ID。 */
    private String userId;

    /** 会话标题，取首条用户消息。 */
    private String title;
}
