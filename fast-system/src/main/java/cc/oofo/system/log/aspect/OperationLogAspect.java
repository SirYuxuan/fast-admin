package cc.oofo.system.log.aspect;

import java.sql.Timestamp;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.entity.SysOperationLog;
import cc.oofo.system.log.service.SysOperationLogService;
import cc.oofo.utils.ServletUtil;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 操作日志切面：拦截带 @OperationLog 的方法，自动记录日志。
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogService logService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable thrown = null;
        try {
            result = point.proceed();
            return result;
        } catch (Throwable t) {
            thrown = t;
            throw t;
        } finally {
            try {
                saveLog(point, operationLog, result, thrown, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("save operation log failed", e);
            }
        }
    }

    @Async
    void saveLog(ProceedingJoinPoint point, OperationLog ann, Object result,
                 Throwable thrown, long cost) {
        SysOperationLog entity = new SysOperationLog();

        // 方法信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        entity.setMethod(signature.getDeclaringType().getSimpleName()
                + "." + signature.getName());
        entity.setTitle(ann.title());
        entity.setBusinessType(ann.type().name());
        entity.setOperatorType("ADMIN");

        // 请求信息
        HttpServletRequest req = ServletUtil.getRequest();
        if (req != null) {
            entity.setRequestMethod(req.getMethod());
            entity.setUrl(req.getRequestURI());
        }
        entity.setIp(ServletUtil.getClientIp());

        // 用户信息
        try {
            if (StpUtil.isLogin()) {
                entity.setUserId(StpUtil.getLoginIdAsString());
            }
        } catch (Exception ignored) {
        }

        // 请求参数
        if (ann.saveRequest()) {
            entity.setRequestParams(safeJson(filterArgs(point.getArgs())));
        }
        // 响应结果
        if (ann.saveResponse() && thrown == null) {
            String json = safeJson(result);
            if (json != null && json.length() > 2000) {
                json = json.substring(0, 2000) + "...(truncated)";
            }
            entity.setResponseResult(json);
        }

        // 状态
        if (thrown != null) {
            entity.setStatus(0);
            entity.setErrorMsg(thrown.getMessage());
        } else {
            entity.setStatus(1);
        }

        entity.setCostTime(cost);
        entity.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        logService.save(entity);
    }

    /** 过滤掉不应该序列化的参数（File/Response 等） */
    private Object[] filterArgs(Object[] args) {
        if (args == null) return new Object[0];
        Object[] out = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object a = args[i];
            if (a instanceof MultipartFile) {
                out[i] = "[MultipartFile]";
            } else if (a instanceof jakarta.servlet.ServletRequest
                    || a instanceof jakarta.servlet.ServletResponse) {
                out[i] = a.getClass().getSimpleName();
            } else {
                out[i] = a;
            }
        }
        return out;
    }

    private String safeJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
