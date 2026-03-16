package cc.oofo.system.menu.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import cc.oofo.system.menu.entity.enums.SysMenuStatus;
import cc.oofo.system.menu.entity.enums.SysMenuType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统菜单实体类
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_menu")
public class SysMenu extends BaseEntity<SysMenu> {

    /**
     * 父菜单ID，顶级菜单为NULL
     */
    private String pid;

    /**
     * 菜单/按钮名称
     */
    private String name;

    /**
     * 菜单/按钮编码，用于权限控制
     */
    private String code;

    /**
     * 菜单类型：1=menu, 2=catalog, 3=button, 4=embedded, 5=link
     */
    private SysMenuType type;

    /**
     * 状态：1启用，0禁用
     */
    private SysMenuStatus status;

    /**
     * 前端路由路径
     */
    private String path;

    /**
     * 激活路径
     */
    private String activePath;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 菜单/按钮图标
     */
    private String icon;

    /**
     * 激活状态图标
     */
    private String metaActiveIcon;

    /**
     * 前端显示标题，可用于国际化
     */
    private String metaTitle;

    /**
     * 菜单排序，数值越小越靠前
     */
    private Integer metaOrder;

    /**
     * 是否固定标签页
     */
    private Boolean metaAffixTab;

    /**
     * 是否保持组件活跃状态
     */
    private Boolean metaKeepAlive;

    /**
     * 是否在菜单中隐藏
     */
    private Boolean metaHideInMenu;

    /**
     * 是否隐藏子菜单
     */
    private Boolean metaHideChildrenInMenu;

    /**
     * 是否在面包屑中隐藏
     */
    private Boolean metaHideInBreadcrumb;

    /**
     * 是否在标签页中隐藏
     */
    private Boolean metaHideInTab;

    /**
     * 徽标显示内容
     */
    private String metaBadge;

    /**
     * 徽标类型，如 normal/dot
     */
    private String metaBadgeType;

    /**
     * 徽标颜色或样式
     */
    private String metaBadgeVariants;

    /**
     * 内嵌 IFrame 链接
     */
    private String metaIframeSrc;

    /**
     * 外部链接地址
     */
    private String metaLink;

    /**
     * 备注
     */
    private String remark;

}