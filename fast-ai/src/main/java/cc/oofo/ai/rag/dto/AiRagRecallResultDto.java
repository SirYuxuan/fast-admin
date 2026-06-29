package cc.oofo.ai.rag.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * 知识库召回测试结果。
 */
@Data
@Builder
public class AiRagRecallResultDto {

    private String query;

    private Integer topK;

    private Long latencyMs;

    private List<Item> items;

    @Data
    @Builder
    public static class Item {

        private String chunkId;

        private String documentId;

        private String fileName;

        private Integer chunkIndex;

        private Double score;

        private String content;
    }
}
