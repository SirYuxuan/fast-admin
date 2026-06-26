package cc.oofo.ai.mcp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外部 MCP 服务配置。
 *
 * <p>当前版本只负责配置维护；Agent 接入 MCP 工具时会按 enabled=true 的记录加载。</p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_mcp_server")
public class AiMcpServer extends BaseEntity<AiMcpServer> {

    /** MCP 服务名称。 */
    private String name;

    /** 传输类型：stdio / sse / streamable-http。 */
    private String transport;

    /** stdio 模式的启动命令。 */
    private String command;

    /** sse / streamable-http 模式的服务地址。 */
    private String url;

    /** stdio 命令参数 JSON 数组。 */
    private String argsJson;

    /** 远程连接请求头 JSON 对象。 */
    private String headersJson;

    /** 是否启用。 */
    private Boolean enabled;

    /** SSE 连接保活开关（仅 transport=sse 有意义）。 */
    private Boolean keepAlive;

    /** 保活间隔（秒）。 */
    private Integer keepAliveInterval;

    /** 关联的保活定时任务（sys_job）主键。 */
    private String keepAliveJobId;

    /** 备注。 */
    private String remark;

    /** 当前运行时是否已连接。 */
    @TableField(exist = false)
    private Boolean connected;

    /** 当前连接暴露的工具数量。 */
    @TableField(exist = false)
    private Integer toolCount;

    /** 当前连接暴露的提示词数量。 */
    @TableField(exist = false)
    private Integer promptCount;

    /** 当前连接暴露的资源数量。 */
    @TableField(exist = false)
    private Integer resourceCount;

    /** 估算上下文占用 token 数。 */
    @TableField(exist = false)
    private Integer contextTokenCount;

    /** 当前连接状态说明。 */
    @TableField(exist = false)
    private String statusMessage;
}
