package cc.oofo.system.menu.entity.dto;

import java.util.List;

import lombok.Data;

/**
 * 系统菜单 DTO
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Data
public class SysMenuDto {

    /**
     * 菜单ID
     */
    private String id;

    /**
     * 父菜单ID
     */
    private String pid;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 权限编码
     */
    private String authCode;

    /**
     * 菜单状态：1启用，0禁用
     */
    private Integer status;

    /**
     * 菜单类型：menu/catalog/button/embedded/link
     */
    private String type;

    /**
     * 前端路由路径
     */
    private String path;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 菜单元数据
     */
    private MenuMeta meta;

    /**
     * 子菜单列表
     */
    private List<SysMenuDto> children;

    /**
     * 菜单元数据内部类
     */
    @Data
    public static class MenuMeta {

        /**
         * 菜单图标
         */
        private String icon;

        /**
         * 激活图标
         */
        private String activeIcon;

        /**
         * 当前激活的菜单路径（停留在隐藏路由时，用于高亮指定的父级菜单）
         */
        private String activePath;

        /**
         * 菜单标题（支持国际化key）
         */
        private String title;

        /**
         * 菜单排序
         */
        private Integer order;

        /**
         * 是否固定标签页
         */
        private Boolean affixTab;

        /**
         * 是否缓存页面
         */
        private Boolean keepAlive;

        /**
         * 是否在菜单中隐藏（路由仍注册，可通过跳转访问）
         */
        private Boolean hideInMenu;

        /**
         * 是否隐藏子菜单
         */
        private Boolean hideChildrenInMenu;

        /**
         * 是否在面包屑中隐藏
         */
        private Boolean hideInBreadcrumb;

        /**
         * 是否在标签页中隐藏
         */
        private Boolean hideInTab;

        /**
         * 徽标内容
         */
        private String badge;

        /**
         * 徽标类型：normal/dot
         */
        private String badgeType;

        /**
         * 徽标样式：primary/success/warning/error等
         */
        private String badgeVariants;

        /**
         * 内嵌iframe地址
         */
        private String iframeSrc;

        /**
         * 外部链接地址
         */
        private String link;
    }

}