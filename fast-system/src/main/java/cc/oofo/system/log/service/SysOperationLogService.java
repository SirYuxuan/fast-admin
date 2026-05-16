package cc.oofo.system.log.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.framework.core.service.BaseService;
import cc.oofo.system.log.entity.SysOperationLog;
import cc.oofo.system.log.entity.query.SysOperationLogQuery;

/**
 * 操作日志服务
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Service
public class SysOperationLogService extends BaseService<SysOperationLog> {

    public Page<SysOperationLog> page(SysOperationLogQuery query) {
        query.getQueryWrapper().orderByDesc("created_at");
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    /** 清空所有操作日志 */
    public void clean() {
        baseMapper.delete(null);
    }
}
