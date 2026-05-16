package cc.oofo.system.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity<SysDictData> {

    /** 字典类型 */
    private String dictType;
    /** 字典标签 */
    private String dictLabel;
    /** 字典键值 */
    private String dictValue;
    /** 排序 */
    private Integer dictSort;
    /** 样式属性（颜色等） */
    private String cssClass;
    /** 表格回显样式 */
    private String listClass;
    /** 是否默认 */
    private Integer isDefault;
    /** 状态 */
    private Integer status;
    /** 备注 */
    private String remark;
}
