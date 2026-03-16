package cc.oofo.system.user.entity.enums;

import java.util.Objects;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 系统用户性别枚举
 *
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
public enum SysUserSex {

    /**
     * 男
     */
    MALE(1, "男"),
    /**
     * 女
     */
    FEMALE(2, "女"),
    /**
     * 未知
     */
    UNKNOWN(0, "未知");

    @EnumValue
    @JsonValue
    private final Integer value;
    private String name;

    SysUserSex(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Integer value() {
        return this.value;
    }

    public boolean equals(Integer status) {
        return Objects.equals(status, this.value);
    }

}