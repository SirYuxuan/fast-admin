package cc.oofo.system.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity<SysDictType> {

    /** 字典名称 */
    private String dictName;
    /** 字典类型（编码） */
    private String dictType;
    /** 状态：1启用 0禁用 */
    private Integer status;
    /** 备注 */
    private String remark;
}
