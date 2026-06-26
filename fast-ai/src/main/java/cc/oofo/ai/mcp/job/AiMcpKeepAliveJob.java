package cc.oofo.ai.mcp.job;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import cc.oofo.ai.mcp.service.AiMcpClientManager;
import lombok.RequiredArgsConstructor;

/**
 * SSE MCP 连接保活任务。
 *
 * <p>由系统定时任务（sys_job）按服务各自配置的间隔反射调用：
 * bean=aiMcpKeepAliveJob, method=ping, params=&lt;MCP 服务 id&gt;。
 * 每个开启保活的 SSE 服务对应一条 sys_job，互不影响。</p>
 */
@Component("aiMcpKeepAliveJob")
@RequiredArgsConstructor
public class AiMcpKeepAliveJob {

    private final AiMcpClientManager mcpClientManager;

    /**
     * 对单个 MCP 服务发送一次保活探测。
     *
     * @param serverId MCP 服务主键（来自 sys_job.method_params）
     */
    public void ping(String serverId) {
        if (!StringUtils.hasText(serverId)) {
            return;
        }
        mcpClientManager.pingServer(serverId);
    }
}
