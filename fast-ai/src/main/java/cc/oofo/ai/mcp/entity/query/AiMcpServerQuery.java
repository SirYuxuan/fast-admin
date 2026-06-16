package cc.oofo.ai.mcp.entity.query;

import cc.oofo.ai.mcp.entity.AiMcpServer;
import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.query.annotation.Operator;
import cc.oofo.framework.core.query.annotation.QueryField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MCP 服务配置查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AiMcpServerQuery extends BaseQuery<AiMcpServer> {

    @QueryField(operator = Operator.LIKE)
    private String name;

    private String transport;

    private Boolean enabled;
}
