package cc.oofo.framework.exception;

/**
 * 业务异常
 *
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
public class BizException extends RuntimeException {

    /**
     * 创建异常
     * 
     * @param message 异常信息
     */
    public BizException(String message) {
        super(message);
    }

    /**
     * 创建异常
     * 
     * @param template 异常信息模板
     * @param args     参数
     */
    public BizException(String template, Object... args) {
        super(String.format(template, args));
    }

    /**
     * 创建异常
     * 
     * @param message 异常信息
     * @return 异常s
     */
    public static BizException of(String message) {
        return new BizException(message);
    }
}
