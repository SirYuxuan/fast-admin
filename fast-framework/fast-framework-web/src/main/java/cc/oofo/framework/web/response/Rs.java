package cc.oofo.framework.web.response;

import lombok.Data;

/**
 * 统一响应对象
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Data
public class Rs<T> {
    /**
     * 状态码
     */
    private int code;
    /**
     * 消息
     */
    private String message;
    /**
     * 数据
     */
    private Object data;

    /**
     * 成功响应
     * 
     * @param <T> 数据类型
     * @return 统一响应对象
     */
    public static <T> Rs<T> ok() {
        Rs<T> rs = new Rs<>();
        rs.setCode(HttpCode.SUCCESS);
        rs.setMessage("success");
        return rs;
    }

    /**
     * 成功响应
     * 
     * @param <T>     数据类型
     * @param message 消息
     * @return 统一响应对象
     */
    public static <T> Rs<T> ok(String message) {
        Rs<T> rs = ok();
        rs.setMessage(message);
        return rs;
    }

    /**
     * 成功响应
     * 
     * @param <T>  数据类型
     * @param data 数据
     * @return 统一响应对象
     */
    public static <T> Rs<T> ok(T data) {
        Rs<T> rs = ok();
        rs.setData(data);
        return rs;
    }

    /**
     * 错误响应
     * 
     * @param message 消息
     * @param <T>     数据类型
     * @return 统一响应对象
     */
    public static <T> Rs<String> error(String message) {
        Rs<String> rs = new Rs<>();
        rs.setCode(HttpCode.ERROR);
        rs.setMessage(message);
        return rs;
    }

    /**
     * 错误响应
     * 
     * @param <T> 数据类型
     * @return 统一响应对象
     */
    public static Rs<String> error() {
        return error("服务器异常，请联系管理员");
    }
}