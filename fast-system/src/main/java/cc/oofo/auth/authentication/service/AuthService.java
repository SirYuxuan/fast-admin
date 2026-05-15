package cc.oofo.auth.authentication.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cc.oofo.auth.authentication.dto.LoginDto;
import cc.oofo.framework.exception.BizException;
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

    /**
     * 用户登录
     * 
     * @param loginDto 登录参数
     * @return token
     */
    public String login(LoginDto loginDto) {
        // 1. 获取用户信息
        AuthUserDto authUser = sysUserApi.getAuthUser(loginDto.getUsername());
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
        return StpUtil.getTokenValue();
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