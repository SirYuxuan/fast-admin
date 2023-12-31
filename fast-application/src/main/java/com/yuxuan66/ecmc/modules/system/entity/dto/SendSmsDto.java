package com.yuxuan66.ecmc.modules.system.entity.dto;

import lombok.Data;

/**
 * 发送短信dto
 * @author Sir丶雨轩
 * @since 2023/9/5
 */
@Data
public class SendSmsDto extends BaseImgCaptchaDto{

    /**
     * 手机号
     */
    private String phone;

}
