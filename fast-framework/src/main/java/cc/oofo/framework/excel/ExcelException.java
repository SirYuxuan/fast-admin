package cc.oofo.framework.excel;

import cc.oofo.framework.exception.BizException;

/**
 * Excel 处理异常
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
public class ExcelException extends BizException {
    public ExcelException(String message) {
        super(message);
    }
    public ExcelException(String message, Throwable cause) {
        super(message);
    }
}
