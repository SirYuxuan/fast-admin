package cc.oofo.ai.rag.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cc.oofo.ai.rag.entity.AiKnowledgeBase;

@Mapper
public interface AiKnowledgeBaseMapper extends BaseMapper<AiKnowledgeBase> {
}
