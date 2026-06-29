package cc.oofo.ai.rag.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cc.oofo.ai.rag.dto.AiKnowledgeBaseSaveDto;
import cc.oofo.ai.rag.dto.AiRagVectorStoreStatusDto;
import cc.oofo.ai.rag.dto.AiRagRecallRequest;
import cc.oofo.ai.rag.dto.AiRagRecallResultDto;
import cc.oofo.ai.rag.entity.AiKnowledgeBase;
import cc.oofo.ai.rag.entity.AiKnowledgeChunk;
import cc.oofo.ai.rag.entity.AiKnowledgeDocument;
import cc.oofo.ai.rag.entity.query.AiKnowledgeBaseQuery;
import cc.oofo.ai.rag.entity.query.AiKnowledgeChunkQuery;
import cc.oofo.ai.rag.entity.query.AiKnowledgeDocumentQuery;
import cc.oofo.ai.rag.service.AiKnowledgeBaseService;
import cc.oofo.ai.rag.service.AiRagQdrantService;
import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

/**
 * AI 知识库接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/rag")
public class AiRagController {

    private final AiRagQdrantService qdrantService;
    private final AiKnowledgeBaseService knowledgeBaseService;

    @GetMapping("/vector-store/status")
    public Rs<AiRagVectorStoreStatusDto> vectorStoreStatus() {
        return Rs.ok(qdrantService.status());
    }

    @GetMapping("/vector-store/collections")
    public Rs<List<String>> collections() {
        return Rs.ok(qdrantService.collections());
    }

    @GetMapping("/knowledge")
    public Ps<AiKnowledgeBase> page(AiKnowledgeBaseQuery query) {
        return Ps.ok(knowledgeBaseService.page(query));
    }

    @GetMapping("/knowledge/{id}")
    public Rs<AiKnowledgeBase> detail(@PathVariable String id) {
        return Rs.ok(knowledgeBaseService.getByIdOrThrow(id));
    }

    @PostMapping("/knowledge")
    @OperationLog(title = "AI 知识库", type = BusinessType.CREATE)
    public Rs<Void> add(@RequestBody AiKnowledgeBaseSaveDto dto) {
        knowledgeBaseService.add(dto);
        return Rs.ok();
    }

    @PutMapping("/knowledge")
    @OperationLog(title = "AI 知识库", type = BusinessType.UPDATE)
    public Rs<Void> update(@RequestBody AiKnowledgeBaseSaveDto dto) {
        knowledgeBaseService.update(dto);
        return Rs.ok();
    }

    @PostMapping("/knowledge/{id}/enabled")
    @OperationLog(title = "AI 知识库", type = BusinessType.UPDATE)
    public Rs<Void> changeEnabled(@PathVariable String id, @RequestParam boolean enabled) {
        knowledgeBaseService.changeEnabled(id, enabled);
        return Rs.ok();
    }

    @DeleteMapping("/knowledge/{id}")
    @OperationLog(title = "AI 知识库", type = BusinessType.DELETE)
    public Rs<Void> deleteKnowledge(@PathVariable String id) {
        knowledgeBaseService.del(id);
        return Rs.ok();
    }

    @GetMapping("/documents")
    public Ps<AiKnowledgeDocument> documentPage(AiKnowledgeDocumentQuery query) {
        return Ps.ok(knowledgeBaseService.documentPage(query));
    }

    @GetMapping("/documents/{id}")
    public Rs<AiKnowledgeDocument> documentDetail(@PathVariable String id) {
        return Rs.ok(knowledgeBaseService.getDocumentDetail(id));
    }

    @GetMapping("/documents/{id}/chunks")
    public Ps<AiKnowledgeChunk> chunkPage(@PathVariable String id, AiKnowledgeChunkQuery query) {
        query.setDocumentId(id);
        return Ps.ok(knowledgeBaseService.chunkPage(query));
    }

    @PostMapping("/knowledge/{id}/documents/upload")
    @OperationLog(title = "AI 知识库文档", type = BusinessType.CREATE)
    public Rs<AiKnowledgeDocument> upload(@PathVariable String id, @RequestPart("file") MultipartFile file) {
        return Rs.ok(knowledgeBaseService.uploadDocument(id, file));
    }

    @PostMapping("/documents/{id}/reindex")
    @OperationLog(title = "AI 知识库文档", type = BusinessType.UPDATE)
    public Rs<Void> reindex(@PathVariable String id) {
        knowledgeBaseService.reindexDocument(id);
        return Rs.ok();
    }

    @DeleteMapping("/documents/{id}")
    @OperationLog(title = "AI 知识库文档", type = BusinessType.DELETE)
    public Rs<Void> deleteDocument(@PathVariable String id,
            @RequestParam(defaultValue = "false") boolean deleteSourceFile) {
        knowledgeBaseService.deleteDocument(id, deleteSourceFile);
        return Rs.ok();
    }

    @PostMapping("/recall-test")
    public Rs<AiRagRecallResultDto> recallTest(@RequestBody AiRagRecallRequest request) {
        return Rs.ok(knowledgeBaseService.recall(request));
    }
}
