package cc.oofo.system.user.service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.menu.entity.SysMenu;
import cc.oofo.system.menu.mapper.SysMenuMapper;
import cc.oofo.system.user.api.SysUserApi;
import cc.oofo.system.user.dto.AuthUserDto;
import cc.oofo.system.user.dto.SysUserInfoDto;
import cc.oofo.system.user.entity.SysUser;
import cc.oofo.system.user.entity.dto.SysUserDto;
import cc.oofo.system.user.entity.query.SysUserQuery;
import cc.oofo.system.user.mapper.SysUserMapper;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;

/**
 * 系统用户服务实现类
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Service
public class SysUserService extends BaseService<SysUser> implements SysUserApi {

    @Resource
    private SysMenuMapper sysMenuMapper;

    /**
     * 获取登录用户信息
     * 
     * @return 用户信息
     */
    public SysUserInfoDto info() {
        // 获取当前登录用户w
        SysUser user = getById(StpUtil.getLoginIdAsString());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        SysUserInfoDto userInfoDto = new SysUserInfoDto();
        BeanUtils.copyProperties(user, userInfoDto);
        return userInfoDto;
    }

    /**
     * 获取用户列表
     * 
     * @param query 查询参数
     * @return 用户列表
     */
    @SuppressWarnings("null")
    public IPage<SysUserDto> list(SysUserQuery query) {
        QueryWrapper<SysUser> queryWrapper = query.getQueryWrapper();
        Page<SysUser> page = ((SysUserMapper) baseMapper).selectPage(query.getMPPage(), queryWrapper);
        return page.convert(new Function<SysUser, SysUserDto>() {
            @Override
            public SysUserDto apply(SysUser item) {
                SysUserDto dto = new SysUserDto();
                BeanUtils.copyProperties(item, dto);
                return dto;
            }
        });
    }

    /**
     * SQL 查询用户列表（带角色）
     *
     * @param query 查询参数
     * @return 用户列表（非分页）
     */
    public List<SysUserDto> listUsers(SysUserQuery query) {
        return ((SysUserMapper) baseMapper).listUserWithRoles(query);
    }

    /**
     * SQL 统计用户数量
     *
     * @param query 查询参数
     * @return 总数
     */
    public Long countUsers(SysUserQuery query) {
        return ((SysUserMapper) baseMapper).countUserWithRoles(query);
    }

    /**
     * 修改一个用户
     * 
     * @param sysUserDto 用户数据传输对象
     */
    public void edit(SysUserDto sysUserDto) {
        if (sysUserDto.getId() == null) {
            throw new BizException("用户ID不能为空");
        }
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserDto, sysUser);
        updateById(sysUser);
    }

    /**
     * 添加一个用户
     * 
     * @param sysUserDto 用户数据传输对象
     */
    public void add(SysUserDto sysUserDto) {
        if (sysUserDto == null) {
            throw new BizException("用户信息不能为空");
        }
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserDto, sysUser);
        save(sysUser);
    }

    /**
     * 删除一个用户
     * 
     * @param id 用户ID
     */
    public void del(String id) {
        removeById(id);
    }

    /**
     * 根据用户名获取认证用户信息
     * 
     * @param username 用户名
     * @return 认证用户信息
     */
    @Override
    public AuthUserDto getAuthUser(String username) {
        SysUser user = query().eq("username", username).one();
        if (user == null) {
            return null;
        }

        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setId(user.getId());
        authUserDto.setUsername(user.getUsername());
        authUserDto.setPassword(user.getPassword());
        authUserDto.setStatus(user.getStatus());
        authUserDto.setNickname(user.getNickname());
        return authUserDto;
    }

    /**
     * 获取用户权限编码列表
     * 
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @Override
    public List<String> getUserPermissionCodes(String userId) {
        List<SysMenu> menuList = sysMenuMapper.selectAllByUserId(userId);
        return menuList.stream()
                .map(SysMenu::getCode)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

}