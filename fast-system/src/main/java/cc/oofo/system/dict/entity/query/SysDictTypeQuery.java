package cc.oofo.system.dict.entity.query;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import cc.oofo.system.dict.entity.SysDictType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SysDictTypeQuery extends BaseQuery<SysDictType> {

    @QueryField(operator = Operator.LIKE)
    private String dictName;

    @QueryField(operator = Operator.LIKE)
    private String dictType;

    private Integer status;
}
