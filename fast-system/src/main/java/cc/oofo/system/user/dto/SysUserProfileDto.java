package cc.oofo.system.user.dto;

import lombok.Data;

/**
 * 个人中心 - 基本信息更新 DTO
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Data
public class SysUserProfileDto {

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

}
