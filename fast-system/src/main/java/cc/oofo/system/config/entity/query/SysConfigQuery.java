package cc.oofo.system.config.entity.query;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import cc.oofo.system.config.entity.SysConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SysConfigQuery extends BaseQuery<SysConfig> {

    @QueryField(operator = Operator.LIKE)
    private String configName;

    @QueryField(operator = Operator.LIKE)
    private String configKey;

    private Integer configType;
}
