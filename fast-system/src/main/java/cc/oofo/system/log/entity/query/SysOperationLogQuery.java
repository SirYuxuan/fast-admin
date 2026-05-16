package cc.oofo.system.log.entity.query;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import cc.oofo.system.log.entity.SysOperationLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SysOperationLogQuery extends BaseQuery<SysOperationLog> {

    @QueryField(operator = Operator.LIKE)
    private String title;

    private String businessType;

    @QueryField(operator = Operator.LIKE)
    private String username;

    private Integer status;
}
