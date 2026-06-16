package cc.oofo.ai.mcp.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.oofo.ai.mcp.entity.AiMcpServer;
import cc.oofo.framework.exception.BizException;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpClientTransport;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MCP 客户端生命周期管理。
 *
 * <p>启动时按 enabled=true 的配置建立连接，将其工具并入 ChatClient 工具集。
 * 单个连接失败只记录告警，不影响内置工具与对话主链路。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiMcpClientManager implements DisposableBean {

    private final AiMcpServerService mcpServerService;
    private final ObjectMapper objectMapper;

    private volatile List<McpSyncClient> activeClients = List.of();

    @PostConstruct
    public void init() {
        try {
            reload();
        } catch (Exception e) {
            log.warn("MCP client initialization failed at startup (will retry on next reload): {}", e.getMessage());
        }
    }

    /**
     * 重新加载所有已启用的 MCP 服务器。关闭旧连接后重建，v1 不支持热插拔。
     */
    public synchronized void reload() {
        closeAll();
        List<AiMcpServer> servers = mcpServerService.listEnabled();
        List<McpSyncClient> clients = new ArrayList<>();
        for (AiMcpServer server : servers) {
            try {
                McpSyncClient client = buildClient(server);
                clients.add(client);
                log.info("MCP server connected: {}", server.getName());
            } catch (Exception e) {
                log.warn("MCP server '{}' failed to connect, skipped: {}", server.getName(), e.getMessage());
            }
        }
        activeClients = Collections.unmodifiableList(clients);
    }

    /**
     * 返回所有已连接 MCP 服务器提供的工具回调列表。
     */
    public List<ToolCallback> listToolCallbacks() {
        List<McpSyncClient> clients = activeClients;
        if (clients.isEmpty()) {
            return List.of();
        }
        try {
            ToolCallback[] callbacks = new SyncMcpToolCallbackProvider(clients).getToolCallbacks();
            return Arrays.asList(callbacks);
        } catch (Exception e) {
            log.warn("Failed to list MCP tool callbacks: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void destroy() {
        closeAll();
    }

    private synchronized void closeAll() {
        for (McpSyncClient client : activeClients) {
            try {
                client.close();
            } catch (Exception e) {
                log.debug("Error closing MCP client", e);
            }
        }
        activeClients = List.of();
    }

    private McpSyncClient buildClient(AiMcpServer server) {
        McpSyncClient client = McpClient.sync(createTransport(server))
                .requestTimeout(Duration.ofSeconds(30))
                .build();
        client.initialize();
        return client;
    }

    private McpClientTransport createTransport(AiMcpServer server) {
        return switch (server.getTransport()) {
            case "stdio" -> createStdioTransport(server);
            case "sse", "streamable-http" -> createHttpTransport(server);
            default -> throw new BizException("不支持的 MCP 传输类型: " + server.getTransport());
        };
    }

    private StdioClientTransport createStdioTransport(AiMcpServer server) {
        List<String> args = parseJsonArray(server.getArgsJson());
        ServerParameters params = ServerParameters.builder(server.getCommand())
                .args(args)
                .build();
        return new StdioClientTransport(params, McpJsonDefaults.getMapper());
    }

    private HttpClientSseClientTransport createHttpTransport(AiMcpServer server) {
        HttpClientSseClientTransport.Builder builder = HttpClientSseClientTransport.builder(server.getUrl());
        Map<String, String> headers = parseJsonObject(server.getHeadersJson());
        if (!headers.isEmpty()) {
            java.net.http.HttpRequest.Builder rb = java.net.http.HttpRequest.newBuilder();
            headers.forEach(rb::header);
            builder.requestBuilder(rb);
        }
        return builder.build();
    }

    private List<String> parseJsonArray(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            List<String> result = new ArrayList<>();
            if (node.isArray()) {
                for (JsonNode el : node) {
                    result.add(el.asText());
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to parse MCP args JSON: {}", json);
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseJsonObject(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse MCP headers JSON: {}", json);
            return Map.of();
        }
    }
}
