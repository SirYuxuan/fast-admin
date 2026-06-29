package cc.oofo.ai.rag.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import cc.oofo.ai.rag.event.AiKnowledgeDocumentIndexEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 知识库文档异步索引监听器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiKnowledgeDocumentIndexListener {

    private final AiKnowledgeBaseService knowledgeBaseService;

    @Async("aiRagIndexExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onIndexRequested(AiKnowledgeDocumentIndexEvent event) {
        try {
            knowledgeBaseService.indexDocument(event.documentId());
        } catch (Exception e) {
            log.warn("AI 知识库文档异步索引失败，documentId={}，原因：{}", event.documentId(), e.getMessage());
        }
    }
}
