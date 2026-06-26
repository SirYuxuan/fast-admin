package cc.oofo.auth.security.filter;

import java.util.Set;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import cc.oofo.auth.security.WebSecurityConfig.DemoProperties;
import cc.oofo.framework.exception.BizException;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 演示模式下禁止业务写操作，避免演示环境数据被修改。
 */
@Component
@RequiredArgsConstructor
public class DemoModeInterceptor implements HandlerInterceptor {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final DemoProperties demoProperties;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        if (!demoProperties.isEnabled() || request.getDispatcherType() != DispatcherType.REQUEST) {
            return true;
        }
        if (WRITE_METHODS.contains(request.getMethod())) {
            throw new BizException(demoProperties.getMessage());
        }
        return true;
    }
}
