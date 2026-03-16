package cc.oofo.system.menu.entity.enums;

import java.util.Objects;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 系统菜单状态枚举
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
public enum SysMenuStatus {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),
    /**
     * 启用
     */
    ENABLED(1, "启用");

    @EnumValue
    private final Integer value;
    private final String name;

    SysMenuStatus(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public Integer value() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }

    public boolean equals(Integer status) {
        return Objects.equals(status, this.value);
    }

}