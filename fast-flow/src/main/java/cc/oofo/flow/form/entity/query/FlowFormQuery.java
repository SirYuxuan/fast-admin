package cc.oofo.flow.form.entity.query;

import cc.oofo.flow.form.entity.FlowForm;
import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义表单查询条件。
 *
 * @author Sir丶雨轩
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FlowFormQuery extends BaseQuery<FlowForm> {

    @QueryField(operator = Operator.LIKE)
    private String name;

    @QueryField(operator = Operator.LIKE)
    private String formKey;
}
