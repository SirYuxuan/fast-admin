package cc.oofo.ai.rag.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 知识库切片元数据。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_knowledge_chunk")
public class AiKnowledgeChunk extends BaseEntity<AiKnowledgeChunk> {

    private String knowledgeBaseId;

    private String documentId;

    private String pointId;

    private Integer chunkIndex;

    private Integer tokenCount;

    private String content;
}
