package cc.oofo.ai.rag.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 知识库。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_knowledge_base")
public class AiKnowledgeBase extends BaseEntity<AiKnowledgeBase> {

    private String name;

    private String description;

    private Boolean enabled;

    private Integer chunkSize;

    private Integer chunkOverlap;

    private String chunkDelimiter;

    private Integer documentCount;

    private Integer chunkCount;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime lastIndexedAt;

    private String remark;
}
