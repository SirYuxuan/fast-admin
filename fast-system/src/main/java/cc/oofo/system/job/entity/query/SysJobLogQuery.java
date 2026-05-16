package cc.oofo.system.job.entity.query;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import cc.oofo.system.job.entity.SysJobLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SysJobLogQuery extends BaseQuery<SysJobLog> {

    private String jobId;

    @QueryField(operator = Operator.LIKE)
    private String jobName;

    private Integer status;
}
