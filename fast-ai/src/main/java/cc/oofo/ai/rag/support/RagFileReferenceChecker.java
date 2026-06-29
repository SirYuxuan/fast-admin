package cc.oofo.ai.rag.support;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import cc.oofo.ai.rag.entity.AiKnowledgeDocument;
import cc.oofo.ai.rag.mapper.AiKnowledgeDocumentMapper;
import cc.oofo.system.file.spi.FileReferenceChecker;
import lombok.RequiredArgsConstructor;

/**
 * AI 知识库文件引用检查：被知识库文档引用的源文件禁止在文件管理里直接删除。
 *
 * @author Sir丶雨轩
 */
@Component
@RequiredArgsConstructor
public class RagFileReferenceChecker implements FileReferenceChecker {

    private final AiKnowledgeDocumentMapper documentMapper;

    @Override
    public Optional<String> checkReference(String fileId) {
        List<AiKnowledgeDocument> docs = documentMapper.selectList(
                new LambdaQueryWrapper<AiKnowledgeDocument>()
                        .eq(AiKnowledgeDocument::getFileId, fileId)
                        .select(AiKnowledgeDocument::getFileName));
        if (docs.isEmpty()) {
            return Optional.empty();
        }
        String name = docs.get(0).getFileName();
        return Optional.of("该文件已被 AI 知识库文档「" + name + "」引用，请先在知识库中删除对应文档后再删除源文件");
    }
}
