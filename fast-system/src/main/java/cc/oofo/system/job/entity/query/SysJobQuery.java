package cc.oofo.system.job.entity.query;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import cc.oofo.system.job.entity.SysJob;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SysJobQuery extends BaseQuery<SysJob> {

    @QueryField(operator = Operator.LIKE)
    private String jobName;

    private String jobGroup;
    private Integer status;
}
