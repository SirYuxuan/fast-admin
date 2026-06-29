package cc.oofo.ai.rag.entity.query;

import cc.oofo.ai.rag.entity.AiKnowledgeChunk;
import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 知识库切片查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AiKnowledgeChunkQuery extends BaseQuery<AiKnowledgeChunk> {

    @QueryField(operator = Operator.EQ, prop = "document_id")
    private String documentId;

    @QueryField(operator = Operator.LIKE)
    private String content;
}
