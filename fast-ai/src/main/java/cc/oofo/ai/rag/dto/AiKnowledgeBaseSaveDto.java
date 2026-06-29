package cc.oofo.ai.rag.dto;

import lombok.Data;

/**
 * AI 知识库保存 DTO。
 */
@Data
public class AiKnowledgeBaseSaveDto {

    private String id;

    private String name;

    private String description;

    private Boolean enabled;

    private Integer chunkSize;

    private Integer chunkOverlap;

    private String chunkDelimiter;

    private String remark;
}
