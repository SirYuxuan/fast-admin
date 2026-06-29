package cc.oofo.ai.rag.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 知识库文档。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_knowledge_document")
public class AiKnowledgeDocument extends BaseEntity<AiKnowledgeDocument> {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_INDEXING = "indexing";
    public static final String STATUS_INDEXED = "indexed";
    public static final String STATUS_FAILED = "failed";

    private String knowledgeBaseId;

    private String fileId;

    private String fileName;

    private String contentType;

    private Long fileSize;

    private String status;

    private Integer chunkCount;

    /**
     * 索引错误信息。重新索引成功时需要把该列清空，因此用 ALWAYS 策略，
     * 否则 MyBatis-Plus 默认 NOT_NULL 策略会跳过 null 值，导致旧错误残留（已索引却仍显示报错）。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String errorMsg;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime indexedAt;
}
