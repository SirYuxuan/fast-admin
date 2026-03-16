package cc.oofo.system.user.api;

import java.util.List;

import cc.oofo.system.user.dto.AuthUserDto;

/**
 * 系统用户服务接口
 * 
 * @since 2025/11/14
 */
public interface SysUserApi {

    /**
     * 根据用户名获取认证用户信息
     * 
     * @param username 用户名
     * @return 认证用户信息
     */
    AuthUserDto getAuthUser(String username);

    /**
     * 获取用户权限编码列表
     * 
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> getUserPermissionCodes(String userId);

}