package cc.oofo.flow.model.entity.query;

import cc.oofo.flow.model.entity.FlowModel;
import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程模型查询条件。
 *
 * @author Sir丶雨轩
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FlowModelQuery extends BaseQuery<FlowModel> {

    @QueryField(operator = Operator.LIKE)
    private String name;

    @QueryField(operator = Operator.LIKE)
    private String modelKey;

    private String category;

    private Integer status;
}
