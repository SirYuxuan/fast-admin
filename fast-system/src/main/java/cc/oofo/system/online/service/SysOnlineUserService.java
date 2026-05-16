package cc.oofo.system.online.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import cc.oofo.framework.exception.BizException;
import cc.oofo.system.online.dto.OnlineUserDto;
import cc.oofo.system.user.entity.SysUser;
import cc.oofo.system.user.service.SysUserService;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 在线用户服务（基于 Sa-Token）。
 *
 * 关键 API：
 *  - StpUtil.searchTokenValue(prefix, start, size, sortType)：分页查 token
 *  - StpUtil.getLoginIdByToken(token)：token → userId
 *  - StpUtil.kickout(userId)：强制下线（清掉该用户所有 token）
 *  - StpUtil.logoutByTokenValue(token)：注销单个 token
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOnlineUserService {

    private final SysUserService userService;

    /** 列出所有在线用户（按登录时间倒序） */
    public List<OnlineUserDto> list(String keyword) {
        // 拉所有 token（最多取 1000，足够运维场景）
        List<String> tokens = StpUtil.searchTokenValue("", 0, 1000, false);
        List<OnlineUserDto> result = new ArrayList<>();
        for (String fullToken : tokens) {
            try {
                // Sa-Token 返回的 token 是 "satoken:login:token:xxxxxxxx" 这种带前缀的
                String tokenValue = fullToken.contains(":") ?
                        fullToken.substring(fullToken.lastIndexOf(":") + 1) : fullToken;
                Object loginId = StpUtil.getLoginIdByToken(tokenValue);
                if (loginId == null) continue;
                String userId = loginId.toString();

                SysUser user = userService.getById(userId);
                if (user == null) continue;

                if (keyword != null && !keyword.isBlank()) {
                    String kw = keyword.toLowerCase();
                    boolean hit = (user.getUsername() != null && user.getUsername().toLowerCase().contains(kw))
                            || (user.getNickname() != null && user.getNickname().toLowerCase().contains(kw));
                    if (!hit) continue;
                }

                OnlineUserDto dto = new OnlineUserDto();
                dto.setTokenValue(tokenValue);
                dto.setUserId(userId);
                dto.setUsername(user.getUsername());
                dto.setNickname(user.getNickname());
                dto.setLoginIp(user.getLoginIp());
                dto.setTokenTimeout(StpUtil.getTokenTimeout(tokenValue));

                // 从 TokenSession 取登录设备信息（如果有保存的话）
                try {
                    SaSession session = StpUtil.getTokenSessionByToken(tokenValue);
                    if (session != null) {
                        dto.setBrowser((String) session.get("browser"));
                        dto.setOs((String) session.get("os"));
                        Object t = session.get("loginTime");
                        if (t instanceof Long l) dto.setLoginTime(l);
                    }
                } catch (Exception ignored) {
                }
                // 若 session 没记录登录时间，用 user.loginTime
                if (dto.getLoginTime() == null && user.getLoginTime() != null) {
                    dto.setLoginTime(user.getLoginTime().getTime());
                }
                result.add(dto);
            } catch (Exception e) {
                log.warn("parse online token failed: {}", e.getMessage());
            }
        }
        result.sort(Comparator.comparing(
                (OnlineUserDto d) -> d.getLoginTime() == null ? 0L : d.getLoginTime()
        ).reversed());
        return result;
    }

    /** 强制下线（按 tokenValue 注销单个会话） */
    public void kickout(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) {
            throw new BizException("token 不能为空");
        }
        try {
            StpUtil.logoutByTokenValue(tokenValue);
        } catch (Exception e) {
            log.error("kickout failed", e);
            throw new BizException("强制下线失败：" + e.getMessage());
        }
    }
}
