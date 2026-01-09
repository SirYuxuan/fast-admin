package cc.oofo.auth.security.filter;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import cc.oofo.utils.RedisUtil;
import cc.oofo.utils.constants.RedisKeys;
import cc.oofo.utils.context.AuditContext;
import cc.oofo.utils.context.AuditContextHolder;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 将登录用户信息写入审计上下文，供持久层自动填充使用。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class AuditContextFilter implements HandlerInterceptor {

    private static final String DEFAULT_USER_ID = "NOT_LOGIN";
    private static final String DEFAULT_USER_NAME = "系统管理员";

    private final RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (StpUtil.isLogin()) {
            String userId = StpUtil.getLoginIdAsString();
            String nickname = DEFAULT_USER_NAME;
            Object cachedNickName = redisUtil.getVal(RedisKeys.SYSTEM_USER_NICKNAME_PREFIX + userId);
            if (cachedNickName != null) {
                nickname = cachedNickName.toString();
            }
            AuditContextHolder.set(new AuditContext(userId, nickname));
        } else {
            AuditContextHolder.set(new AuditContext(DEFAULT_USER_ID, DEFAULT_USER_NAME));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        AuditContextHolder.clear();
    }
}
