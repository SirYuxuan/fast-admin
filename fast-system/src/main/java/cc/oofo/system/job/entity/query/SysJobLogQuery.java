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

    @QueryField(prop = "job_id")
    private String jobId;

    @QueryField(prop = "job_name", operator = Operator.LIKE)
    private String jobName;

    @QueryField
    private Integer status;
}
