package cc.oofo.system.menu.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.menu.entity.SysMenu;
import cc.oofo.system.menu.entity.dto.SysMenuDto;
import cc.oofo.system.menu.entity.dto.SysMenuSaveDto;
import cc.oofo.system.menu.entity.enums.SysMenuStatus;
import cc.oofo.system.menu.entity.enums.SysMenuType;
import cc.oofo.system.menu.mapper.SysMenuMapper;
import cc.oofo.system.permission.entity.SysRolesMenus;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;

/**
 * 系统菜单服务类
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Service
@Transactional
public class SysMenuService extends BaseService<SysMenu> {

    @Resource
    private SysMenuMapper sysMenuMapper;

    /**
     * 获取当前用户的菜单树
     * 
     * @param isButton 是否包含按钮类型菜单
     * @return 菜单树列表
     */
    public List<SysMenuDto> userMenu() {
        // 查询当前用户有权限的所有菜单
        List<SysMenu> menuList = sysMenuMapper.selectAllByUserId(StpUtil.getLoginIdAsString());

        // 转换为DTO并构建树形结构
        return buildMenuTree(menuList, false);
    }

    /**
     * 添加菜单
     * 
     * @param menuSaveDto 菜单保存DTO
     */
    public void add(SysMenuSaveDto menuSaveDto) {
        // 1. 参数校验
        validateMenuData(menuSaveDto, null);

        // 2. 创建菜单实体
        SysMenu menu = new SysMenu();
        convertSaveDtoToEntity(menuSaveDto, menu);

        // 3. 保存菜单
        save(menu);
    }

    /**
     * 更新菜单
     * 
     * @param menuSaveDto 菜单保存DTO
     */
    public void update(SysMenuSaveDto menuSaveDto) {
        // 1. 检查菜单是否存在
        if (!StringUtils.hasText(menuSaveDto.getId())) {
            throw new BizException("菜单ID不能为空");
        }

        SysMenu existMenu = getById(menuSaveDto.getId());
        if (existMenu == null) {
            throw new BizException("菜单不存在");
        }

        // 2. 参数校验
        validateMenuData(menuSaveDto, menuSaveDto.getId());

        // 3. 更新菜单实体
        convertSaveDtoToEntity(menuSaveDto, existMenu);

        // 4. 保存更新
        updateById(existMenu);
    }

    /**
     * 校验菜单数据
     * 
     * @param menuSaveDto 菜单保存DTO
     * @param menuId      菜单ID（更新时使用）
     */
    private void validateMenuData(SysMenuSaveDto menuSaveDto, String menuId) {
        // 校验必填字段
        if (!StringUtils.hasText(menuSaveDto.getName())) {
            throw new BizException("菜单名称不能为空");
        }

        if (!StringUtils.hasText(menuSaveDto.getType())) {
            throw new BizException("菜单类型不能为空");
        }

        // 校验菜单名称唯一性
        if (nameExists(menuId, menuSaveDto.getName())) {
            throw new BizException("菜单名称已存在");
        }

        // 校验路径唯一性（非按钮类型且路径不为空时）
        if (StringUtils.hasText(menuSaveDto.getPath()) &&
                !"button".equalsIgnoreCase(menuSaveDto.getType()) &&
                pathExists(menuId, menuSaveDto.getPath())) {
            throw new BizException("菜单路径已存在");
        }

        // 校验父菜单是否存在
        if (StringUtils.hasText(menuSaveDto.getPid())) {
            SysMenu parentMenu = getById(menuSaveDto.getPid());
            if (parentMenu == null) {
                throw new BizException("父菜单不存在");
            }

            // 不能选择自己作为父菜单
            if (Objects.equals(menuSaveDto.getPid(), menuId)) {
                throw new BizException("不能选择自己作为父菜单");
            }
        }
    }

    /**
     * 将保存DTO转换为实体
     * 
     * @param menuSaveDto 菜单保存DTO
     * @param menu        菜单实体
     */
    private void convertSaveDtoToEntity(SysMenuSaveDto menuSaveDto, SysMenu menu) {
        // 基础属性
        menu.setPid(menuSaveDto.getPid());
        menu.setName(menuSaveDto.getName());
        menu.setCode(menuSaveDto.getAuthCode());
        menu.setPath(menuSaveDto.getPath());
        menu.setActivePath(menuSaveDto.getActivePath());
        menu.setComponent(menuSaveDto.getComponent());
        menu.setRemark(menuSaveDto.getRemark());

        // 转换菜单类型枚举
        if (StringUtils.hasText(menuSaveDto.getType())) {
            try {
                menu.setType(SysMenuType.valueOf(menuSaveDto.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BizException("无效的菜单类型：" + menuSaveDto.getType());
            }
        }

        // 转换状态枚举
        if (menuSaveDto.getStatus() != null) {
            menu.setStatus(menuSaveDto.getStatus() == 1 ? SysMenuStatus.ENABLED : SysMenuStatus.DISABLED);
        }

        // 处理meta信息
        SysMenuSaveDto.MenuMetaSave meta = menuSaveDto.getMeta();
        if (meta != null) {
            menu.setIcon(meta.getIcon());
            menu.setMetaActiveIcon(meta.getActiveIcon());
            menu.setMetaTitle(meta.getTitle());
            menu.setMetaOrder(meta.getOrder());
            menu.setMetaAffixTab(meta.getAffixTab());
            menu.setMetaKeepAlive(meta.getKeepAlive());
            menu.setMetaHideInMenu(meta.getHideInMenu());
            menu.setMetaHideChildrenInMenu(meta.getHideChildrenInMenu());
            menu.setMetaHideInBreadcrumb(meta.getHideInBreadcrumb());
            menu.setMetaHideInTab(meta.getHideInTab());
            menu.setMetaBadge(meta.getBadge());
            menu.setMetaBadgeType(meta.getBadgeType());
            menu.setMetaBadgeVariants(meta.getBadgeVariants());
            menu.setMetaIframeSrc(meta.getIframeSrc());
            menu.setMetaLink(meta.getLink());
        }
    }

    /**
     * 检查菜单名称是否存在
     * 
     * @param id   菜单ID
     * @param name 菜单名称
     * @return 是否存在
     */
    public Boolean nameExists(String id, String name) {
        return query().ne(StringUtils.hasText(id), "id", id).eq("name", name).exists();
    }

    /**
     * 检查菜单路径是否存在
     * 
     * @param id   菜单ID
     * @param path 菜单路径
     * @return 是否存在
     */
    public Boolean pathExists(String id, String path) {
        return query().ne(StringUtils.hasText(id), "id", id).eq("path", path).exists();
    }

    /**
     * 删除菜单
     * 
     * @param id 菜单ID
     * @return 删除结果
     */
    public void del(String id) {
        // 删除菜单及其子菜单。有子菜单不让删除
        if (query().eq("pid", id).exists()) {
            throw new BizException("该菜单存在子菜单，无法删除");
        }
        removeById(id);
        // 删除角色关系
        new SysRolesMenus().delete(new QueryWrapper<SysRolesMenus>().eq("menu_id", id));
    }

    /**
     * 构建菜单树
     * 
     * @param menuList 菜单列表
     * @return 菜单树
     */
    public List<SysMenuDto> buildMenuTree(List<SysMenu> menuList, Boolean isButton) {
        if (menuList == null || menuList.isEmpty()) {
            return new ArrayList<>();
        }

        // 过滤掉button类型的菜单，转换为DTO并按ID分组，便于查找
        Map<String, SysMenuDto> menuMap = menuList.stream()
                .filter(menu -> menu.getType() != null
                        && (isButton != null && isButton || !menu.getType().name().equalsIgnoreCase("BUTTON")))
                .map(this::convertToDto)
                .collect(Collectors.toMap(SysMenuDto::getId, dto -> dto));

        // 构建树形结构
        List<SysMenuDto> rootMenus = new ArrayList<>();

        for (SysMenuDto menu : menuMap.values()) {
            if (StringUtils.hasText(menu.getPid())) {
                // 有父菜单，添加到父菜单的children中
                SysMenuDto parent = menuMap.get(menu.getPid());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            } else {
                // 顶级菜单
                rootMenus.add(menu);
            }
        }

        // 对每个层级进行排序
        sortMenus(rootMenus);
        return rootMenus;
    }

    /**
     * 将SysMenu转换为SysMenuDto
     * 
     * @param menu 菜单实体
     * @return 菜单DTO
     */
    private SysMenuDto convertToDto(SysMenu menu) {

        if (menu == null) {
            throw new BizException("菜单不存在");
        }

        SysMenuDto dto = new SysMenuDto();
        BeanUtils.copyProperties(menu, dto);

        // 设置type字段（枚举转字符串）
        if (menu.getType() != null) {
            dto.setType(menu.getType().name().toLowerCase());
        }

        // 设置status字段
        if (menu.getStatus() != null) {
            dto.setStatus(menu.getStatus().value());
        }

        // 构建meta对象
        SysMenuDto.MenuMeta meta = new SysMenuDto.MenuMeta();
        meta.setIcon(menu.getIcon());
        meta.setTitle(menu.getMetaTitle());
        meta.setOrder(menu.getMetaOrder());
        meta.setAffixTab(menu.getMetaAffixTab());
        meta.setBadge(menu.getMetaBadge());
        meta.setBadgeType(menu.getMetaBadgeType());
        meta.setBadgeVariants(menu.getMetaBadgeVariants());
        meta.setIframeSrc(menu.getMetaIframeSrc());
        meta.setLink(menu.getMetaLink());

        dto.setMeta(meta);
        dto.setAuthCode(menu.getCode()); // 权限编码

        return dto;
    }

    /**
     * 递归排序菜单 从菜单的order字段进行排序，按照升序排列从小到大
     * 
     * @param menus 菜单列表
     */
    private void sortMenus(List<SysMenuDto> menus) {
        if (menus == null || menus.isEmpty()) {
            return;
        }

        // 按order排序，没有order的放最后
        menus.sort((a, b) -> {
            Integer orderA = (a.getMeta() != null && a.getMeta().getOrder() != null)
                    ? a.getMeta().getOrder()
                    : Integer.MAX_VALUE;
            Integer orderB = (b.getMeta() != null && b.getMeta().getOrder() != null)
                    ? b.getMeta().getOrder()
                    : Integer.MAX_VALUE;

            int result = orderA.compareTo(orderB);
            if (result == 0) {
                // order相同时按id排序
                return a.getId().compareTo(b.getId());
            }
            return result;
        });

        // 递归排序子菜单
        for (SysMenuDto menu : menus) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                sortMenus(menu.getChildren());
            }
        }
    }

}