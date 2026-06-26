package cc.oofo.ai.agent.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.ai.agent.entity.AiToolCallLog;
import cc.oofo.ai.agent.entity.query.AiToolCallLogQuery;
import cc.oofo.framework.core.service.BaseService;
import lombok.RequiredArgsConstructor;

/**
 * AI 工具调用审计日志查询服务（只读）。
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AiToolCallLogService extends BaseService<AiToolCallLog> {

    public Page<AiToolCallLog> page(AiToolCallLogQuery query) {
        query.getQueryWrapper().orderByDesc("created_at");
        return page(query.getMPPage(), query.getQueryWrapper());
    }
}
