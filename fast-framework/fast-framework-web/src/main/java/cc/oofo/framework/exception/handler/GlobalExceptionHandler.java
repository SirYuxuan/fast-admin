package cc.oofo.framework.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import cc.oofo.framework.exception.BizException;
import cc.oofo.framework.web.response.HttpCode;
import cc.oofo.framework.web.response.Rs;
import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器
 *
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 接口不存在提示
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Rs<String>> handleResourceNotFoundException(NoResourceFoundException e) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, Rs.error("您请求的资源: " + e.getResourcePath() + " 不存在"));
    }

    /**
     * 处理所有接口数据验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Rs<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return buildResponseEntity(Rs.error(message));
    }

    /**
     * 处理所有业务异常
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Rs<String>> notLoginException(BizException e) {
        return buildResponseEntity(Rs.error(e.getMessage()));
    }

    /**
     * 没有登录
     *
     * @param e 错误信息
     * @return 没有登录
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Rs<String>> notLoginException(NotLoginException e) {
        Rs<String> error = Rs.error("您还没有登录,请先登录系统");
        error.setCode(HttpCode.UNAUTHORIZED);
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, error);
    }

    /**
     * 处理所有不可知的异常
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Rs<String>> handleException(Throwable e) {
        String message = e.getMessage();
        log.error(message, e);

        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, Rs.error(("服务器异常,请稍后重试...")));
    }

    /**
     * 统一返回
     */
    private ResponseEntity<Rs<String>> buildResponseEntity(Rs<String> respEntity) {
        return buildResponseEntity(HttpStatus.OK, respEntity);
    }

    /**
     * 统一返回
     * 
     * @param httpStatus http状态码
     * @param respEntity 返回实体
     * @return 响应实体
     */
    @SuppressWarnings("null")
    private ResponseEntity<Rs<String>> buildResponseEntity(HttpStatus httpStatus, Rs<String> respEntity) {
        return new ResponseEntity<>(respEntity, httpStatus);
    }
}
