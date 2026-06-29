package cc.oofo.ai.rag.entity.query;

import cc.oofo.ai.rag.entity.AiKnowledgeDocument;
import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 知识库文档查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AiKnowledgeDocumentQuery extends BaseQuery<AiKnowledgeDocument> {

    @QueryField(operator = Operator.EQ, prop = "knowledge_base_id")
    private String knowledgeBaseId;

    @QueryField(operator = Operator.LIKE, prop = "file_name")
    private String fileName;

    @QueryField(operator = Operator.EQ)
    private String status;
}
