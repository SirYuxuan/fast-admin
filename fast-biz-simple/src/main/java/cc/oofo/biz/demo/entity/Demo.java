package cc.oofo.biz.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 示例实体（复制本模块作为新业务起点时，从这里开始重命名）
 *
 * @author Sir丶雨轩
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("biz_demo")
public class Demo extends BaseEntity<Demo> {

    /** 名称 */
    private String name;

    /** 备注 */
    private String remark;

    /** 是否启用 */
    private Boolean isEnabled;

}
