package cc.oofo.ai.agent.entity.query;

import cc.oofo.ai.agent.entity.AiToolCallLog;
import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 工具调用审计日志查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AiToolCallLogQuery extends BaseQuery<AiToolCallLog> {

    @QueryField(operator = Operator.LIKE)
    private String toolName;

    @QueryField(operator = Operator.EQ)
    private String source;

    @QueryField(operator = Operator.EQ)
    private Boolean success;

    @QueryField(operator = Operator.EQ)
    private String sessionId;

    @QueryField(operator = Operator.EQ)
    private String operatorId;
}
