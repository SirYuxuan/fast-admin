package cc.oofo.biz.demo.entity.query;

import cc.oofo.biz.demo.entity.Demo;
import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 示例查询实体
 *
 * @author Sir丶雨轩
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DemoQuery extends BaseQuery<Demo> {

    @QueryField(operator = Operator.LIKE)
    private String name;

    private Boolean isEnabled;

}
