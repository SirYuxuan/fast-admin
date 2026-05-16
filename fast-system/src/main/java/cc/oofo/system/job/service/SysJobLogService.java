package cc.oofo.system.job.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.framework.core.service.BaseService;
import cc.oofo.system.job.entity.SysJobLog;
import cc.oofo.system.job.entity.query.SysJobLogQuery;

/**
 * 定时任务日志服务
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Service
public class SysJobLogService extends BaseService<SysJobLog> {

    public Page<SysJobLog> page(SysJobLogQuery query) {
        query.getQueryWrapper().orderByDesc("created_at");
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    public void clean() {
        baseMapper.delete(null);
    }
}
