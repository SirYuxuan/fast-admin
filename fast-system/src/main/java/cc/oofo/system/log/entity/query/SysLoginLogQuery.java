package cc.oofo.system.log.entity.query;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import cc.oofo.system.log.entity.SysLoginLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SysLoginLogQuery extends BaseQuery<SysLoginLog> {

    @QueryField(operator = Operator.LIKE)
    private String username;

    @QueryField(operator = Operator.LIKE)
    private String ip;

    private Integer status;
    private String type;
}
