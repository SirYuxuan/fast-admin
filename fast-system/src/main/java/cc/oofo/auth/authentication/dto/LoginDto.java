package cc.oofo.auth.authentication.dto;

import lombok.Data;

/**
 * 登录DTO
 *
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Data
public class LoginDto {

    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 验证码
     */
    private String captcha;
    /**
     * 验证码唯一标识
     */
    private String uuid;
    /**
     * 用户唯一标识 <br>
     * 模拟用户登录使用
     */
    private String uid;
}