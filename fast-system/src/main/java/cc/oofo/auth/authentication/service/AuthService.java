package cc.oofo.auth.authentication.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cc.oofo.auth.authentication.dto.LoginDto;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.log.service.SysLoginLogService;
import cc.oofo.system.user.api.SysUserApi;
import cc.oofo.system.user.dto.AuthUserDto;
import cc.oofo.utils.PasswordUtil;
import cc.oofo.utils.RedisUtil;
import cc.oofo.utils.constants.RedisKeys;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;

/**
 * 鉴权服务
 *
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserApi sysUserApi;
    private final RedisUtil redisUtil;
    private final SysLoginLogService loginLogService;

    /**
     * 用户登录
     *
     * @param loginDto 登录参数
     * @return token
     */
    public String login(LoginDto loginDto) {
        String username = loginDto.getUsername();
        try {
            // 1. 获取用户信息
            AuthUserDto authUser = sysUserApi.getAuthUser(username);
            if (authUser == null) {
                throw new BizException("用户名或密码错误");
            }

            // 2. 验证密码
            if (!PasswordUtil.verify(loginDto.getPassword(), authUser.getPassword())) {
                throw new BizException("用户名或密码错误");
            }

            // 3. 验证用户状态
            if (!authUser.isStatusValid()) {
                throw new BizException(authUser.getStatusMessage());
            }

            // 4. 生成 token
            StpUtil.login(authUser.getId());
            // 5. 保存用户昵称
            redisUtil.setVal(RedisKeys.SYSTEM_USER_NICKNAME_PREFIX + authUser.getId(),
                    authUser.getNickname());

            // 6. 记录登录成功日志
            loginLogService.record(authUser.getId(), username, "LOGIN", 1, "登录成功");
            return StpUtil.getTokenValue();
        } catch (BizException e) {
            // 记录登录失败日志
            loginLogService.record(null, username, "LOGIN", 0, e.getMessage());
            throw e;
        }
    }

    /**
     * 用户登出
     */
    public void logout() {
        String userId = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsString();
            }
        } catch (Exception ignored) {
        }
        StpUtil.logout();
        loginLogService.record(userId, null, "LOGOUT", 1, "登出成功");
    }

    /**
     * 获取当前用户的权限编码列表
     * 
     * @return 权限编码列表
     */
    public List<String> codes() {
        return sysUserApi.getUserPermissionCodes(StpUtil.getLoginIdAsString());
    }
}