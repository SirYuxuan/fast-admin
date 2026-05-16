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
     * 记录登录日志。
     *
     * 必须在调用方（主线程）执行，因为依赖 HttpServletRequest 上下文。
     * 内部把信息装好后丢给 @Async 方法异步落库。
     */
    public void record(String userId, String username, String type,
                       Integer status, String msg) {
        // ★ 必须在主线程读取请求信息（ThreadLocal）
        String ip = ServletUtil.getClientIp();
        String ua = ServletUtil.getUserAgent();
        String browser = ServletUtil.parseBrowser(ua);
        String os = ServletUtil.parseOs(ua);
        String device = ua != null && ua.toLowerCase().contains("mobile") ? "Mobile" : "PC";

        saveAsync(userId, username, type, status, msg, ip, browser, os, device);
    }

    /**
     * 真正的落库逻辑，异步执行。
     */
    @Async
    public void saveAsync(String userId, String username, String type,
                          Integer status, String msg,
                          String ip, String browser, String os, String device) {
        try {
            SysLoginLog entity = new SysLoginLog();
            entity.setUserId(userId);
            entity.setUsername(username);
            entity.setType(type);
            entity.setStatus(status);
            entity.setMsg(msg);
            entity.setIp(ip);
            entity.setBrowser(browser);
            entity.setOs(os);
            entity.setDevice(device);
            entity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            save(entity);
        } catch (Exception e) {
            log.error("save login log failed", e);
        }
    }
}
