package cc.oofo.ai.tool.entity.query;

import cc.oofo.ai.tool.entity.AiToolConfig;
import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 工具配置查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AiToolConfigQuery extends BaseQuery<AiToolConfig> {

    @QueryField(operator = Operator.LIKE)
    private String name;

    private String toolCode;

    private String type;

    @QueryField(operator = Operator.EQ)
    private Boolean enabled;
}
