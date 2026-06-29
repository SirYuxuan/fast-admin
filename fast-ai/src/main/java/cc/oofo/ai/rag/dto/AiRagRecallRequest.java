package cc.oofo.ai.rag.dto;

import lombok.Data;

/**
 * 知识库召回测试请求。
 */
@Data
public class AiRagRecallRequest {

    private String knowledgeBaseId;

    private String query;

    private Integer topK;
}
