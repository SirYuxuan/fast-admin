package cc.oofo.ai.model.entity.query;

import cc.oofo.ai.model.entity.AiModelConfig;
import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 模型配置查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AiModelConfigQuery extends BaseQuery<AiModelConfig> {

    @QueryField(operator = Operator.LIKE)
    private String name;

    private String provider;

    private Boolean enabled;

    private Boolean active;
}
