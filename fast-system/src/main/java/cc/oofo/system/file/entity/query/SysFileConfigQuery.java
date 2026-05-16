package cc.oofo.system.file.entity.query;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import cc.oofo.system.file.entity.SysFileConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件存储配置查询
 *
 * @author Sir丶雨轩
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysFileConfigQuery extends BaseQuery<SysFileConfig> {

    @QueryField(operator = Operator.LIKE)
    private String name;

    private String type;

}
