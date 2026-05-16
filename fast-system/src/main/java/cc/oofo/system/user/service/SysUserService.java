package cc.oofo.system.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.utils.PasswordUtil;
import cc.oofo.system.menu.entity.SysMenu;
import cc.oofo.system.menu.mapper.SysMenuMapper;
import cc.oofo.system.permission.entity.SysUsersRoles;
import cc.oofo.system.permission.mapper.SysUsersRolesMapper;
import cc.oofo.system.user.api.SysUserApi;
import cc.oofo.system.user.dto.AuthUserDto;
import cc.oofo.system.user.dto.SysUserInfoDto;
import cc.oofo.system.user.dto.SysUserPasswordDto;
import cc.oofo.system.user.dto.SysUserProfileDto;
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

    @Resource
    private SysUsersRolesMapper sysUsersRolesMapper;

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
     * 更新当前登录用户的个人信息（仅允许修改昵称、邮箱、手机号）
     *
     * @param dto 个人信息 DTO
     */
    @Transactional
    public void updateProfile(SysUserProfileDto dto) {
        String userId = StpUtil.getLoginIdAsString();
        SysUser user = getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        updateById(user);
    }

    /**
     * 修改当前登录用户的头像
     *
     * @param avatar 头像 URL
     */
    @Transactional
    public void changeAvatar(String avatar) {
        if (!StringUtils.hasText(avatar)) {
            throw new BizException("头像地址不能为空");
        }
        String userId = StpUtil.getLoginIdAsString();
        SysUser user = getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setAvatar(avatar);
        updateById(user);
    }

    /**
     * 修改当前登录用户的密码
     *
     * @param dto 密码 DTO
     */
    @Transactional
    public void changePassword(SysUserPasswordDto dto) {
        if (!StringUtils.hasText(dto.getOldPassword())) {
            throw new BizException("旧密码不能为空");
        }
        if (!StringUtils.hasText(dto.getNewPassword())) {
            throw new BizException("新密码不能为空");
        }
        if (dto.getNewPassword().length() < 6) {
            throw new BizException("新密码长度不能小于 6 位");
        }

        String userId = StpUtil.getLoginIdAsString();
        SysUser user = getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (!PasswordUtil.verify(dto.getOldPassword(), user.getPassword())) {
            throw new BizException("旧密码不正确");
        }

        if (PasswordUtil.verify(dto.getNewPassword(), user.getPassword())) {
            throw new BizException("新密码与旧密码相同");
        }

        user.setPassword(PasswordUtil.create(dto.getNewPassword()));
        updateById(user);
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
    @Transactional
    public void edit(SysUserDto sysUserDto) {
        if (sysUserDto.getId() == null) {
            throw new BizException("用户ID不能为空");
        }
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserDto, sysUser);
        updateById(sysUser);
        // 更新角色关联
        if (sysUserDto.getRoles() != null) {
            saveUserRoles(sysUserDto.getId(), sysUserDto.getRoles());
        }
    }

    /**
     * 导出用：根据查询条件返回 SysUser 实体列表
     */
    public List<SysUser> listForExport(SysUserQuery query) {
        return baseMapper.selectList(query.getQueryWrapper());
    }

    /**
     * 批量导入用户（用于 Excel 导入）
     *
     * @param users 用户列表
     * @return 实际新增数量（用户名重复的会跳过）
     */
    @Transactional
    public int batchImport(List<SysUser> users) {
        if (users == null || users.isEmpty()) return 0;
        int count = 0;
        for (SysUser u : users) {
            if (!StringUtils.hasText(u.getUsername())) continue;
            // 用户名已存在则跳过
            if (query().eq("username", u.getUsername()).exists()) continue;
            // 设置默认密码（系统参数 sys.user.initPassword，没配置则用 123456）
            if (!StringUtils.hasText(u.getPassword())) {
                u.setPassword(PasswordUtil.create("123456"));
            } else {
                u.setPassword(PasswordUtil.create(u.getPassword()));
            }
            save(u);
            count++;
        }
        return count;
    }

    /**
     * 添加一个用户
     *
     * @param sysUserDto 用户数据传输对象
     */
    @Transactional
    public void add(SysUserDto sysUserDto) {
        if (sysUserDto == null) {
            throw new BizException("用户信息不能为空");
        }
        if (!StringUtils.hasText(sysUserDto.getPassword())) {
            throw new BizException("密码不能为空");
        }
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserDto, sysUser);
        sysUser.setPassword(PasswordUtil.create(sysUserDto.getPassword()));
        save(sysUser);
        // 保存角色关联
        if (sysUserDto.getRoles() != null && !sysUserDto.getRoles().isEmpty()) {
            saveUserRoles(sysUser.getId(), sysUserDto.getRoles());
        }
    }

    /**
     * 保存用户角色关联（先清后写）
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    private void saveUserRoles(String userId, List<String> roleIds) {
        sysUsersRolesMapper.deleteByUserId(userId);
        if (roleIds.isEmpty()) {
            return;
        }
        List<SysUsersRoles> list = new ArrayList<>();
        for (String roleId : roleIds) {
            SysUsersRoles ur = new SysUsersRoles();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        sysUsersRolesMapper.batchInsert(list);
    }

    /**
     * 删除一个用户
     *
     * @param id 用户ID
     */
    @Transactional
    public void del(String id) {
        removeById(id);
        sysUsersRolesMapper.deleteByUserId(id);
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