package cc.oofo.ai.rag.entity.query;

import cc.oofo.ai.rag.entity.AiKnowledgeBase;
import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 知识库查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AiKnowledgeBaseQuery extends BaseQuery<AiKnowledgeBase> {

    @QueryField(operator = Operator.LIKE)
    private String name;

    @QueryField(operator = Operator.EQ)
    private Boolean enabled;
}
