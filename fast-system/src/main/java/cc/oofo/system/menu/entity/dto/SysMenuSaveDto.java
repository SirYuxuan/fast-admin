package cc.oofo.system.menu.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 系统菜单保存 DTO
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysMenuSaveDto {

    /**
     * 菜单ID（更新时需要）
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
     * 激活路径
     */
    private String activePath;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 菜单元数据
     */
    private MenuMetaSave meta;

    /**
     * 备注
     */
    private String remark;

    /**
     * 菜单保存元数据内部类
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MenuMetaSave {

        /**
         * 菜单标题（支持国际化key）
         */
        private String title;

        /**
         * 菜单图标
         */
        private String icon;

        /**
         * 激活状态图标
         */
        private String activeIcon;

        /**
         * 菜单排序
         */
        private Integer order;

        /**
         * 是否固定标签页
         */
        private Boolean affixTab;

        /**
         * 是否保持组件活跃状态
         */
        private Boolean keepAlive;

        /**
         * 是否在菜单中隐藏
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
         * 徽标样式：primary/success/warning/error/default等
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