package cc.oofo.system.menu.entity.enums;

import java.util.Objects;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 系统菜单类型枚举
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
public enum SysMenuType {

    /**
     * 菜单
     */
    MENU(1, "菜单"),
    /**
     * 目录
     */
    CATALOG(2, "目录"),
    /**
     * 按钮
     */
    BUTTON(3, "按钮"),
    /**
     * 嵌入式
     */
    EMBEDDED(4, "嵌入式"),
    /**
     * 链接
     */
    LINK(5, "链接");

    @EnumValue
    private final Integer value;
    private final String name;

    SysMenuType(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public Integer value() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }

    public boolean equals(Integer type) {
        return Objects.equals(type, this.value);
    }

}