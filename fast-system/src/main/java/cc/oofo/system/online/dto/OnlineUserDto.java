package cc.oofo.system.online.dto;

import lombok.Data;

/**
 * 在线用户 DTO
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Data
public class OnlineUserDto {

    /** Sa-Token tokenValue（用于强制下线） */
    private String tokenValue;
    /** 用户ID */
    private String userId;
    /** 用户名 */
    private String username;
    /** 昵称 */
    private String nickname;
    /** 登录 IP */
    private String loginIp;
    /** 浏览器 */
    private String browser;
    /** 操作系统 */
    private String os;
    /** 登录时间（毫秒） */
    private Long loginTime;
    /** Token 过期剩余秒数（-1 永久，-2 不存在） */
    private Long tokenTimeout;
}
