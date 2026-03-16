package cc.oofo.auth.authentication.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.auth.authentication.dto.LoginDto;
import cc.oofo.auth.authentication.dto.LoginRsDto;
import cc.oofo.auth.authentication.service.AuthService;
import cc.oofo.framework.web.response.Rs;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;

/**
 * 鉴权控制器
 *
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     * 
     * @param loginDto 登录信息
     * @return 结果
     */
    @PostMapping(path = "/login")
    public Rs<LoginRsDto> login(@RequestBody LoginDto loginDto) {
        LoginRsDto loginResult = new LoginRsDto();
        loginResult.setAccessToken(authService.login(loginDto));
        return Rs.ok(loginResult);
    }

    /**
     * 获取当前用户的权限编码列表
     * 
     * @return 权限编码列表
     */
    @GetMapping(path = "/codes")
    public Rs<List<String>> codes() {
        return Rs.ok(authService.codes());
    }

    /**
     * 登出
     * 
     * @return 结果
     */
    @PostMapping(path = "/logout")
    public Rs<Void> logout() {
        StpUtil.logout();
        return Rs.ok();
    }
}
