package cc.oofo.system.user.dto;

import lombok.Data;

/**
 * 个人中心 - 修改密码 DTO
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Data
public class SysUserPasswordDto {

    /** 旧密码 */
    private String oldPassword;

    /** 新密码 */
    private String newPassword;

}
