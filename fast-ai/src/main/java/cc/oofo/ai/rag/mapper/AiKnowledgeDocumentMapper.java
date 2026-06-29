package cc.oofo.ai.rag.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cc.oofo.ai.rag.entity.AiKnowledgeDocument;

@Mapper
public interface AiKnowledgeDocumentMapper extends BaseMapper<AiKnowledgeDocument> {
}
