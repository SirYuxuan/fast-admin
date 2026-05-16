package cc.oofo.system.log.service;

import java.sql.Timestamp;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.framework.core.service.BaseService;
import cc.oofo.system.log.entity.SysLoginLog;
import cc.oofo.system.log.entity.query.SysLoginLogQuery;
import cc.oofo.utils.ServletUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录日志服务
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Slf4j
@Service
public class SysLoginLogService extends BaseService<SysLoginLog> {

    public Page<SysLoginLog> page(SysLoginLogQuery query) {
        query.getQueryWrapper().orderByDesc("created_at");
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    public void clean() {
        baseMapper.delete(null);
    }

    /**
     * 记录登录日志（异步，失败不影响登录）
     */
    @Async
    public void record(String userId, String username, String type,
                       Integer status, String msg) {
        try {
            SysLoginLog log = new SysLoginLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setType(type);
            log.setStatus(status);
            log.setMsg(msg);
            log.setIp(ServletUtil.getClientIp());

            String ua = ServletUtil.getUserAgent();
            log.setBrowser(ServletUtil.parseBrowser(ua));
            log.setOs(ServletUtil.parseOs(ua));
            log.setDevice(ua != null && ua.toLowerCase().contains("mobile") ? "Mobile" : "PC");
            log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            save(log);
        } catch (Exception e) {
            this.log.error("record login log failed", e);
        }
    }
}
