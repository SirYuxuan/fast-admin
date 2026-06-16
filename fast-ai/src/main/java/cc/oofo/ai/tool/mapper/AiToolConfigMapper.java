package cc.oofo.ai.tool.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cc.oofo.ai.tool.entity.AiToolConfig;

/**
 * AI 工具配置 Mapper。
 */
@Mapper
public interface AiToolConfigMapper extends BaseMapper<AiToolConfig> {
}
