package cc.oofo.ai.rag.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.ai.rag.dto.AiKnowledgeBaseSaveDto;
import cc.oofo.ai.rag.dto.AiRagRecallRequest;
import cc.oofo.ai.rag.dto.AiRagRecallResultDto;
import cc.oofo.ai.rag.entity.AiKnowledgeBase;
import cc.oofo.ai.rag.entity.AiKnowledgeChunk;
import cc.oofo.ai.rag.entity.AiKnowledgeDocument;
import cc.oofo.ai.rag.entity.query.AiKnowledgeBaseQuery;
import cc.oofo.ai.rag.entity.query.AiKnowledgeChunkQuery;
import cc.oofo.ai.rag.entity.query.AiKnowledgeDocumentQuery;
import cc.oofo.ai.rag.event.AiKnowledgeDocumentIndexEvent;
import cc.oofo.ai.rag.mapper.AiKnowledgeChunkMapper;
import cc.oofo.ai.rag.mapper.AiKnowledgeDocumentMapper;
import cc.oofo.ai.rag.service.parser.AiDocumentTextExtractor;
import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.file.entity.SysFile;
import cc.oofo.system.file.service.SysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 知识库业务服务。
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AiKnowledgeBaseService extends BaseService<AiKnowledgeBase> {

    private static final int DEFAULT_CHUNK_SIZE = 800;
    private static final int DEFAULT_CHUNK_OVERLAP = 100;
    private static final String DEFAULT_CHUNK_DELIMITER = "\\n\\n";
    private static final int MAX_CHUNK_SIZE = 4000;
    private static final int MAX_TOP_K = 20;
    private static final int CHAT_TOP_K = 4;
    private static final String FILE_BIZ_TYPE = "AI_RAG_DOCUMENT";

    private final SysFileService fileService;
    private final AiKnowledgeDocumentMapper documentMapper;
    private final AiKnowledgeChunkMapper chunkMapper;
    private final AiRagEmbeddingService embeddingService;
    private final AiRagQdrantService qdrantService;
    private final AiRagConfigService ragConfigService;
    private final AiDocumentTextExtractor documentTextExtractor;
    private final ApplicationEventPublisher eventPublisher;

    public Page<AiKnowledgeBase> page(AiKnowledgeBaseQuery query) {
        query.getQueryWrapper().orderByDesc("created_at");
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    public Page<AiKnowledgeDocument> documentPage(AiKnowledgeDocumentQuery query) {
        query.getQueryWrapper().orderByDesc("created_at");
        return documentMapper.selectPage(query.getMPPage(AiKnowledgeDocument.class), query.getQueryWrapper());
    }

    public AiKnowledgeDocument getDocumentDetail(String documentId) {
        return getDocumentOrThrow(documentId);
    }

    public Page<AiKnowledgeChunk> chunkPage(AiKnowledgeChunkQuery query) {
        AiKnowledgeDocument doc = getDocumentOrThrow(query.getDocumentId());
        query.setDocumentId(doc.getId());
        query.getQueryWrapper().orderByAsc("chunk_index");
        return chunkMapper.selectPage(query.getMPPage(AiKnowledgeChunk.class), query.getQueryWrapper());
    }

    public AiKnowledgeBase getByIdOrThrow(String id) {
        AiKnowledgeBase entity = getById(id);
        if (entity == null) {
            throw new BizException("知识库不存在");
        }
        return entity;
    }

    public void add(AiKnowledgeBaseSaveDto dto) {
        validate(dto, null);
        if (nameExists(null, dto.getName())) {
            throw new BizException("知识库名称已存在");
        }
        AiKnowledgeBase entity = new AiKnowledgeBase();
        copyToEntity(dto, entity);
        entity.setEnabled(dto.getEnabled() == null || dto.getEnabled());
        entity.setDocumentCount(0);
        entity.setChunkCount(0);
        save(entity);
    }

    public void update(AiKnowledgeBaseSaveDto dto) {
        AiKnowledgeBase entity = getByIdOrThrow(dto.getId());
        validate(dto, entity.getId());
        if (nameExists(entity.getId(), dto.getName())) {
            throw new BizException("知识库名称已存在");
        }
        copyToEntity(dto, entity);
        updateById(entity);
    }

    public void changeEnabled(String id, boolean enabled) {
        AiKnowledgeBase entity = getByIdOrThrow(id);
        entity.setEnabled(enabled);
        updateById(entity);
    }

    public void del(String id) {
        getByIdOrThrow(id);
        List<AiKnowledgeDocument> docs = documentMapper.selectList(new LambdaQueryWrapper<AiKnowledgeDocument>()
                .eq(AiKnowledgeDocument::getKnowledgeBaseId, id));
        for (AiKnowledgeDocument doc : docs) {
            deleteDocument(doc.getId(), true);
        }
        removeById(id);
    }

    public AiKnowledgeDocument uploadDocument(String knowledgeBaseId, MultipartFile file) {
        ensureRagEnabled();
        AiKnowledgeBase kb = getByIdOrThrow(knowledgeBaseId);
        SysFile savedFile = fileService.upload(file, FILE_BIZ_TYPE, knowledgeBaseId);
        AiKnowledgeDocument doc = new AiKnowledgeDocument();
        doc.setKnowledgeBaseId(kb.getId());
        doc.setFileId(savedFile.getId());
        doc.setFileName(savedFile.getOriginalName());
        doc.setContentType(savedFile.getContentType());
        doc.setFileSize(savedFile.getSize());
        doc.setStatus(AiKnowledgeDocument.STATUS_PENDING);
        doc.setChunkCount(0);
        documentMapper.insert(doc);
        refreshStats(kb.getId());
        publishIndexEvent(doc.getId());
        return documentMapper.selectById(doc.getId());
    }

    public void reindexDocument(String documentId) {
        ensureRagEnabled();
        AiKnowledgeDocument doc = getDocumentOrThrow(documentId);
        doc.setStatus(AiKnowledgeDocument.STATUS_PENDING);
        doc.setChunkCount(0);
        doc.setErrorMsg(null);
        doc.setIndexedAt(null);
        documentMapper.updateById(doc);
        refreshStats(doc.getKnowledgeBaseId());
        publishIndexEvent(doc.getId());
    }

    public void deleteDocument(String documentId, boolean deleteSourceFile) {
        AiKnowledgeDocument doc = getDocumentOrThrow(documentId);
        deleteChunks(doc.getId());
        documentMapper.deleteById(documentId);
        refreshStats(doc.getKnowledgeBaseId());
        // 文档记录已删除，引用解除后再删源文件（否则会被文件引用校验拦截）
        if (deleteSourceFile && StringUtils.hasText(doc.getFileId())) {
            fileService.del(doc.getFileId());
        }
    }

    public AiRagRecallResultDto recall(AiRagRecallRequest request) {
        ensureRagEnabled();
        if (request == null || !StringUtils.hasText(request.getKnowledgeBaseId())) {
            throw new BizException("知识库不能为空");
        }
        if (!StringUtils.hasText(request.getQuery())) {
            throw new BizException("召回问题不能为空");
        }
        AiKnowledgeBase kb = getByIdOrThrow(request.getKnowledgeBaseId());
        if (!Boolean.TRUE.equals(kb.getEnabled())) {
            throw new BizException("知识库已禁用");
        }
        int topK = Math.min(Math.max(request.getTopK() == null ? 5 : request.getTopK(), 1), MAX_TOP_K);
        long start = System.currentTimeMillis();
        List<Double> vector = embeddingService.embed(request.getQuery());
        List<AiRagQdrantService.SearchHit> hits = qdrantService.search(vector, kb.getId(), topK);

        List<String> pointIds = hits.stream().map(AiRagQdrantService.SearchHit::pointId).toList();
        List<AiKnowledgeChunk> chunks = pointIds.isEmpty() ? List.of() : chunkMapper.selectList(
                new LambdaQueryWrapper<AiKnowledgeChunk>().in(AiKnowledgeChunk::getPointId, pointIds));
        Map<String, AiKnowledgeChunk> chunkByPoint = chunks.stream()
                .collect(Collectors.toMap(AiKnowledgeChunk::getPointId, Function.identity()));
        Map<String, AiKnowledgeDocument> docs = loadDocuments(chunks);

        List<AiRagRecallResultDto.Item> items = new ArrayList<>();
        for (AiRagQdrantService.SearchHit hit : hits) {
            AiKnowledgeChunk chunk = chunkByPoint.get(hit.pointId());
            if (chunk == null) {
                continue;
            }
            AiKnowledgeDocument doc = docs.get(chunk.getDocumentId());
            items.add(AiRagRecallResultDto.Item.builder()
                    .chunkId(chunk.getId())
                    .documentId(chunk.getDocumentId())
                    .fileName(doc == null ? null : doc.getFileName())
                    .chunkIndex(chunk.getChunkIndex())
                    .score(hit.score())
                    .content(chunk.getContent())
                    .build());
        }
        return AiRagRecallResultDto.builder()
                .query(request.getQuery())
                .topK(topK)
                .latencyMs(System.currentTimeMillis() - start)
                .items(items)
                .build();
    }

    /**
     * 为聊天检索知识库上下文（RAG）。
     *
     * @param ragMode           检索模式：off 不检索；manual 仅检索指定知识库；其余（auto）检索全部启用的知识库
     * @param knowledgeBaseIds  manual 模式下指定的知识库 ID
     * @param query             用户问题
     * @return 拼好的参考资料上下文与来源文档名；无可用上下文时返回 null
     */
    public ChatContext retrieveChatContext(String ragMode, List<String> knowledgeBaseIds, String query) {
        if ("off".equalsIgnoreCase(ragMode) || !StringUtils.hasText(query) || !ragConfigService.isEnabled()) {
            return null;
        }
        List<AiKnowledgeBase> kbs;
        if ("manual".equalsIgnoreCase(ragMode) && knowledgeBaseIds != null && !knowledgeBaseIds.isEmpty()) {
            kbs = listByIds(knowledgeBaseIds).stream()
                    .filter(kb -> Boolean.TRUE.equals(kb.getEnabled()))
                    .toList();
        } else {
            kbs = list(new LambdaQueryWrapper<AiKnowledgeBase>().eq(AiKnowledgeBase::getEnabled, true));
        }
        if (kbs.isEmpty()) {
            return null;
        }
        try {
            List<Double> vector = embeddingService.embed(query);
            List<AiRagQdrantService.SearchHit> hits = new ArrayList<>();
            for (AiKnowledgeBase kb : kbs) {
                hits.addAll(qdrantService.search(vector, kb.getId(), CHAT_TOP_K));
            }
            if (hits.isEmpty()) {
                return null;
            }
            hits.sort(Comparator.comparingDouble(AiRagQdrantService.SearchHit::score).reversed());
            List<String> pointIds = hits.stream().limit(CHAT_TOP_K)
                    .map(AiRagQdrantService.SearchHit::pointId).toList();
            List<AiKnowledgeChunk> chunks = chunkMapper.selectList(
                    new LambdaQueryWrapper<AiKnowledgeChunk>().in(AiKnowledgeChunk::getPointId, pointIds));
            if (chunks.isEmpty()) {
                return null;
            }
            Map<String, AiKnowledgeChunk> byPoint = chunks.stream()
                    .collect(Collectors.toMap(AiKnowledgeChunk::getPointId, Function.identity()));
            Map<String, AiKnowledgeDocument> docs = loadDocuments(chunks);

            StringBuilder sb = new StringBuilder(
                    "以下是从知识库检索到的参考资料，回答时请优先依据它们；若与问题无关请忽略：\n");
            List<String> sources = new ArrayList<>();
            int idx = 1;
            for (String pointId : pointIds) {
                AiKnowledgeChunk chunk = byPoint.get(pointId);
                if (chunk == null) {
                    continue;
                }
                AiKnowledgeDocument doc = docs.get(chunk.getDocumentId());
                String fileName = doc == null ? "未知文档" : doc.getFileName();
                sb.append("\n[").append(idx++).append("] 来源：").append(fileName).append('\n')
                        .append(chunk.getContent()).append('\n');
                if (!sources.contains(fileName)) {
                    sources.add(fileName);
                }
            }
            return new ChatContext(sb.toString(), sources);
        } catch (Exception e) {
            log.warn("知识库检索失败，本轮对话跳过 RAG：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 聊天 RAG 上下文。
     *
     * @param text    注入提示词的参考资料文本
     * @param sources 命中的来源文档名（去重）
     */
    public record ChatContext(String text, List<String> sources) {
    }

    public void indexDocument(String documentId) {
        AiKnowledgeDocument doc = getDocumentOrThrow(documentId);
        AiKnowledgeBase kb = getByIdOrThrow(doc.getKnowledgeBaseId());
        doc.setStatus(AiKnowledgeDocument.STATUS_INDEXING);
        doc.setErrorMsg(null);
        doc.setIndexedAt(null);
        documentMapper.updateById(doc);
        deleteChunks(doc.getId());

        try {
            String text = extractText(doc);
            List<String> chunks = splitText(text, chunkSize(kb), chunkOverlap(kb), chunkDelimiter(kb));
            if (chunks.isEmpty()) {
                throw new BizException("文档没有可索引文本");
            }
            List<Double> firstVector = embeddingService.embed(chunks.get(0));
            qdrantService.ensureCollection(firstVector.size());
            saveChunk(kb, doc, 0, chunks.get(0), firstVector);
            for (int i = 1; i < chunks.size(); i++) {
                saveChunk(kb, doc, i, chunks.get(i), embeddingService.embed(chunks.get(i)));
            }
            doc.setStatus(AiKnowledgeDocument.STATUS_INDEXED);
            doc.setChunkCount(chunks.size());
            doc.setIndexedAt(LocalDateTime.now());
            documentMapper.updateById(doc);
            refreshStats(kb.getId());
        } catch (Exception e) {
            doc.setStatus(AiKnowledgeDocument.STATUS_FAILED);
            doc.setChunkCount(0);
            doc.setErrorMsg(e.getMessage());
            doc.setIndexedAt(null);
            documentMapper.updateById(doc);
            refreshStats(kb.getId());
        }
    }

    private void saveChunk(AiKnowledgeBase kb, AiKnowledgeDocument doc, int index, String content, List<Double> vector) {
        String pointId = UUID.randomUUID().toString();
        AiKnowledgeChunk chunk = new AiKnowledgeChunk();
        chunk.setKnowledgeBaseId(kb.getId());
        chunk.setDocumentId(doc.getId());
        chunk.setPointId(pointId);
        chunk.setChunkIndex(index);
        chunk.setTokenCount(estimateTokens(content));
        chunk.setContent(content);
        chunkMapper.insert(chunk);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("knowledgeBaseId", kb.getId());
        payload.put("documentId", doc.getId());
        payload.put("chunkId", chunk.getId());
        payload.put("chunkIndex", index);
        payload.put("fileName", doc.getFileName());
        qdrantService.upsert(pointId, vector, payload);
    }

    private String extractText(AiKnowledgeDocument doc) {
        SysFile file = fileService.getById(doc.getFileId());
        if (file == null) {
            throw new BizException("源文件不存在");
        }
        try (InputStream in = fileService.download(file.getId())) {
            return documentTextExtractor.extract(file, in);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("读取文档失败：" + e.getMessage());
        }
    }

    private List<String> splitText(String text, int chunkSize, int overlap, String delimiter) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        List<String> result = StringUtils.hasText(delimiter)
                ? splitByDelimiter(text, chunkSize, delimiter)
                : splitByLength(text, chunkSize, overlap);
        if (!StringUtils.hasText(delimiter) || overlap <= 0 || result.size() <= 1) {
            return result;
        }
        List<String> overlapped = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            String chunk = result.get(i);
            if (i > 0) {
                String prev = result.get(i - 1);
                String prefix = prev.substring(Math.max(0, prev.length() - overlap)).trim();
                if (StringUtils.hasText(prefix)) {
                    chunk = prefix + "\n" + chunk;
                }
            }
            overlapped.add(chunk);
        }
        return overlapped;
    }

    private List<String> splitByDelimiter(String text, int chunkSize, String delimiter) {
        List<String> result = new ArrayList<>();
        String[] parts = text.split(Pattern.quote(delimiter));
        StringBuilder current = new StringBuilder();
        for (String raw : parts) {
            String part = raw.trim();
            if (!StringUtils.hasText(part)) {
                continue;
            }
            if (part.length() > chunkSize) {
                flushChunk(result, current);
                result.addAll(splitByLength(part, chunkSize, 0));
                continue;
            }
            String candidate = current.isEmpty() ? part : current + delimiter + part;
            if (candidate.length() <= chunkSize) {
                current.setLength(0);
                current.append(candidate);
            } else {
                flushChunk(result, current);
                current.append(part);
            }
        }
        flushChunk(result, current);
        if (result.isEmpty()) {
            return splitByLength(text, chunkSize, 0);
        }
        return result;
    }

    private void flushChunk(List<String> result, StringBuilder current) {
        String value = current.toString().trim();
        if (StringUtils.hasText(value)) {
            result.add(value);
        }
        current.setLength(0);
    }

    private List<String> splitByLength(String text, int chunkSize, int overlap) {
        List<String> result = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String chunk = text.substring(start, end).trim();
            if (StringUtils.hasText(chunk)) {
                result.add(chunk);
            }
            if (end >= text.length()) {
                break;
            }
            start = Math.max(end - overlap, start + 1);
        }
        return result;
    }

    private void deleteChunks(String documentId) {
        List<AiKnowledgeChunk> chunks = chunkMapper.selectList(new LambdaQueryWrapper<AiKnowledgeChunk>()
                .eq(AiKnowledgeChunk::getDocumentId, documentId));
        qdrantService.deletePoints(chunks.stream().map(AiKnowledgeChunk::getPointId).toList());
        chunkMapper.delete(new LambdaQueryWrapper<AiKnowledgeChunk>().eq(AiKnowledgeChunk::getDocumentId, documentId));
    }

    private void refreshStats(String knowledgeBaseId) {
        AiKnowledgeBase kb = getById(knowledgeBaseId);
        if (kb == null) {
            return;
        }
        Long docCount = documentMapper.selectCount(new LambdaQueryWrapper<AiKnowledgeDocument>()
                .eq(AiKnowledgeDocument::getKnowledgeBaseId, knowledgeBaseId));
        Long chunkCount = chunkMapper.selectCount(new LambdaQueryWrapper<AiKnowledgeChunk>()
                .eq(AiKnowledgeChunk::getKnowledgeBaseId, knowledgeBaseId));
        LocalDateTime lastIndexedAt = documentMapper.selectList(new LambdaQueryWrapper<AiKnowledgeDocument>()
                .eq(AiKnowledgeDocument::getKnowledgeBaseId, knowledgeBaseId)
                .eq(AiKnowledgeDocument::getStatus, AiKnowledgeDocument.STATUS_INDEXED)
                .isNotNull(AiKnowledgeDocument::getIndexedAt))
                .stream()
                .map(AiKnowledgeDocument::getIndexedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        kb.setDocumentCount(docCount.intValue());
        kb.setChunkCount(chunkCount.intValue());
        kb.setLastIndexedAt(lastIndexedAt);
        updateById(kb);
    }

    private void publishIndexEvent(String documentId) {
        eventPublisher.publishEvent(new AiKnowledgeDocumentIndexEvent(documentId));
    }

    private void ensureRagEnabled() {
        if (!ragConfigService.isEnabled()) {
            throw new BizException("AI 知识库未启用");
        }
    }

    private Map<String, AiKnowledgeDocument> loadDocuments(List<AiKnowledgeChunk> chunks) {
        List<String> docIds = chunks.stream().map(AiKnowledgeChunk::getDocumentId).distinct().toList();
        if (docIds.isEmpty()) {
            return Map.of();
        }
        return documentMapper.selectList(new LambdaQueryWrapper<AiKnowledgeDocument>().in(AiKnowledgeDocument::getId, docIds))
                .stream()
                .collect(Collectors.toMap(AiKnowledgeDocument::getId, Function.identity()));
    }

    private AiKnowledgeDocument getDocumentOrThrow(String id) {
        AiKnowledgeDocument doc = documentMapper.selectById(id);
        if (doc == null) {
            throw new BizException("知识库文档不存在");
        }
        return doc;
    }

    private void validate(AiKnowledgeBaseSaveDto dto, String id) {
        if (dto == null) {
            throw new BizException("知识库不能为空");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new BizException("知识库名称不能为空");
        }
        int chunkSize = dto.getChunkSize() == null ? DEFAULT_CHUNK_SIZE : dto.getChunkSize();
        int overlap = dto.getChunkOverlap() == null ? DEFAULT_CHUNK_OVERLAP : dto.getChunkOverlap();
        if (chunkSize < 100 || chunkSize > MAX_CHUNK_SIZE) {
            throw new BizException("切片长度需在 100~" + MAX_CHUNK_SIZE + " 之间");
        }
        if (overlap < 0 || overlap >= chunkSize) {
            throw new BizException("切片重叠需大于等于 0 且小于切片长度");
        }
        String delimiter = dto.getChunkDelimiter();
        if (StringUtils.hasText(delimiter) && decodeDelimiter(delimiter).length() > 100) {
            throw new BizException("切片分隔符长度不能超过 100");
        }
    }

    private boolean nameExists(String excludeId, String name) {
        LambdaQueryWrapper<AiKnowledgeBase> wrapper = new LambdaQueryWrapper<AiKnowledgeBase>()
                .eq(AiKnowledgeBase::getName, name);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(AiKnowledgeBase::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    private void copyToEntity(AiKnowledgeBaseSaveDto dto, AiKnowledgeBase entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        if (dto.getEnabled() != null) {
        entity.setEnabled(dto.getEnabled());
        }
        entity.setChunkSize(dto.getChunkSize() == null ? DEFAULT_CHUNK_SIZE : dto.getChunkSize());
        entity.setChunkOverlap(dto.getChunkOverlap() == null ? DEFAULT_CHUNK_OVERLAP : dto.getChunkOverlap());
        entity.setChunkDelimiter(StringUtils.hasText(dto.getChunkDelimiter())
                ? dto.getChunkDelimiter().trim()
                : DEFAULT_CHUNK_DELIMITER);
        entity.setRemark(dto.getRemark());
    }

    private int chunkSize(AiKnowledgeBase kb) {
        return kb.getChunkSize() == null ? DEFAULT_CHUNK_SIZE : kb.getChunkSize();
    }

    private int chunkOverlap(AiKnowledgeBase kb) {
        return kb.getChunkOverlap() == null ? DEFAULT_CHUNK_OVERLAP : kb.getChunkOverlap();
    }

    private String chunkDelimiter(AiKnowledgeBase kb) {
        String delimiter = StringUtils.hasText(kb.getChunkDelimiter())
                ? kb.getChunkDelimiter()
                : DEFAULT_CHUNK_DELIMITER;
        return decodeDelimiter(delimiter);
    }

    private String decodeDelimiter(String delimiter) {
        return delimiter
                .replace("\\r", "\r")
                .replace("\\n", "\n")
                .replace("\\t", "\t");
    }

    private int estimateTokens(String text) {
        return Math.max(1, (int) Math.ceil(text.length() / 4.0));
    }
}
