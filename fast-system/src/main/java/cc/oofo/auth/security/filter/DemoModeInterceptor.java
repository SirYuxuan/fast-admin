package cc.oofo.auth.security.filter;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

import org.springframework.lang.Nullable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import cc.oofo.auth.security.WebSecurityConfig.DemoProperties;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.entity.SysOperationLog;
import cc.oofo.system.log.service.SysOperationLogService;
import cc.oofo.utils.ServletUtil;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 演示模式下禁止业务写操作，避免演示环境数据被修改。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoModeInterceptor implements HandlerInterceptor {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final DemoProperties demoProperties;
    private final SysOperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        if (!demoProperties.isEnabled() || request.getDispatcherType() != DispatcherType.REQUEST) {
            return true;
        }
        if (WRITE_METHODS.contains(request.getMethod())) {
            saveBlockedOperationLog(request, handler);
            throw new BizException(demoProperties.getMessage());
        }
        return true;
    }

    private void saveBlockedOperationLog(HttpServletRequest request, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return;
        }
        OperationLog ann = handlerMethod.getMethodAnnotation(OperationLog.class);
        if (ann == null) {
            return;
        }
        try {
            SysOperationLog entity = new SysOperationLog();
            entity.setTitle(ann.title());
            entity.setBusinessType(ann.type().name());
            entity.setMethod(handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName());
            entity.setRequestMethod(request.getMethod());
            entity.setOperatorType("ADMIN");
            entity.setUrl(request.getRequestURI());
            entity.setIp(ServletUtil.getClientIp());
            entity.setUserId(currentUserId());
            if (ann.saveRequest()) {
                entity.setRequestParams(safeJson(request.getParameterMap()));
            }
            entity.setStatus(0);
            entity.setErrorMsg(demoProperties.getMessage());
            entity.setCostTime(0L);
            entity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            operationLogService.save(entity);
        } catch (Exception e) {
            log.error("save demo blocked operation log failed", e);
        }
    }

    @Nullable
    private String currentUserId() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    private String safeJson(Map<String, String[]> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(params);
            return json.length() > 2000 ? json.substring(0, 2000) + "...(truncated)" : json;
        } catch (Exception e) {
            return String.valueOf(params);
        }
    }
}
