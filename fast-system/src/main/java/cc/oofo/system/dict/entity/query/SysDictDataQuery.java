package cc.oofo.system.dict.entity.query;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import cc.oofo.system.dict.entity.SysDictData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SysDictDataQuery extends BaseQuery<SysDictData> {

    private String dictType;

    @QueryField(operator = Operator.LIKE)
    private String dictLabel;

    private Integer status;
}
