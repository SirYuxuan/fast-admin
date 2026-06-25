package cc.oofo.ai.mcp.service;

import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.oofo.ai.agent.dto.AiChatSseEvent;
import cc.oofo.ai.mcp.dto.AiMcpServerInspectDto;
import cc.oofo.ai.mcp.entity.AiMcpServer;
import cc.oofo.framework.exception.BizException;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
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
    private volatile Map<String, McpSyncClient> activeClientMap = Map.of();
    private volatile Map<String, McpServerStatus> serverStatuses = Map.of();
    private volatile Map<String, ManualStreamableServer> manualStreamableServers = Map.of();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ExecutorService initExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "mcp-init");
        t.setDaemon(true);
        return t;
    });

    @PostConstruct
    public void init() {
        // 异步初始化，连接超时/失败不阻塞应用启动
        initExecutor.execute(() -> {
            try {
                reload();
            } catch (Exception e) {
                log.warn("MCP client initialization failed at startup (will retry on next reload): {}", e.getMessage());
            }
        });
    }

    /**
     * 重新加载所有已启用的 MCP 服务器。关闭旧连接后重建，v1 不支持热插拔。
     */
    public synchronized void reload() {
        closeAll();
        List<AiMcpServer> servers = mcpServerService.listEnabled();
        List<McpSyncClient> clients = new ArrayList<>();
        Map<String, McpSyncClient> clientMap = new HashMap<>();
        Map<String, McpServerStatus> statuses = new HashMap<>();
        Map<String, ManualStreamableServer> manualServers = new HashMap<>();
        for (AiMcpServer server : servers) {
            try {
                McpSyncClient client = buildClient(server);
                clients.add(client);
                clientMap.put(server.getId(), client);
                McpServerSnapshot snapshot = inspectClient(client);
                statuses.put(server.getId(), McpServerStatus.connected(snapshot));
                log.info("MCP server connected: {} ({} tools)", server.getName(), snapshot.toolCount());
            } catch (Exception e) {
                if ("streamable-http".equals(server.getTransport())) {
                    try {
                        ManualStreamableServer manualServer = buildManualStreamableServer(server);
                        manualServers.put(server.getId(), manualServer);
                        statuses.put(server.getId(), McpServerStatus.connected(manualServer.snapshot()));
                        log.info("MCP streamable server connected by HTTP fallback: {} ({} tools)",
                                server.getName(), manualServer.snapshot().toolCount());
                        continue;
                    } catch (Exception fallbackError) {
                        statuses.put(server.getId(), McpServerStatus.disconnected(fallbackError.getMessage()));
                        log.warn("MCP server '{}' failed to connect by SDK and fallback: {} / {}",
                                server.getName(), e.getMessage(), fallbackError.getMessage());
                        continue;
                    }
                }
                statuses.put(server.getId(), McpServerStatus.disconnected(e.getMessage()));
                log.warn("MCP server '{}' failed to connect, skipped: {}", server.getName(), e.getMessage());
            }
        }
        activeClients = Collections.unmodifiableList(clients);
        activeClientMap = Collections.unmodifiableMap(clientMap);
        serverStatuses = Collections.unmodifiableMap(statuses);
        manualStreamableServers = Collections.unmodifiableMap(manualServers);
    }

    /**
     * 只重新加载指定 MCP 服务，避免全量重建其它已连接客户端。
     */
    public synchronized void reload(String serverId) {
        AiMcpServer server = mcpServerService.getByIdOrThrow(serverId);
        removeClient(serverId);
        if (!Boolean.TRUE.equals(server.getEnabled())) {
            updateStatus(serverId, McpServerStatus.disconnected("未启用"));
            return;
        }
        try {
            McpSyncClient client = buildClient(server);
            putClient(serverId, client);
            McpServerSnapshot snapshot = inspectClient(client);
            removeManualStreamableServer(serverId);
            updateStatus(serverId, McpServerStatus.connected(snapshot));
            log.info("MCP server reconnected: {} ({} tools)", server.getName(), snapshot.toolCount());
        } catch (Exception e) {
            if ("streamable-http".equals(server.getTransport())) {
                try {
                    ManualStreamableServer manualServer = buildManualStreamableServer(server);
                    putManualStreamableServer(serverId, manualServer);
                    updateStatus(serverId, McpServerStatus.connected(manualServer.snapshot()));
                    log.info("MCP streamable server reconnected by HTTP fallback: {} ({} tools)",
                            server.getName(), manualServer.snapshot().toolCount());
                    return;
                } catch (Exception fallbackError) {
                    updateStatus(serverId, McpServerStatus.disconnected(fallbackError.getMessage()));
                    log.warn("MCP server '{}' failed to reconnect by SDK and fallback: {} / {}",
                            server.getName(), e.getMessage(), fallbackError.getMessage());
                    return;
                }
            }
            updateStatus(serverId, McpServerStatus.disconnected(e.getMessage()));
            log.warn("MCP server '{}' failed to reconnect: {}", server.getName(), e.getMessage());
        }
    }

    public synchronized void remove(String serverId) {
        removeClient(serverId);
        removeManualStreamableServer(serverId);
        Map<String, McpServerStatus> statuses = new HashMap<>(serverStatuses);
        statuses.remove(serverId);
        serverStatuses = Collections.unmodifiableMap(statuses);
    }

    public void applyStatus(AiMcpServer server) {
        if (server == null) {
            return;
        }
        if (!Boolean.TRUE.equals(server.getEnabled())) {
            server.setConnected(false);
            server.setToolCount(0);
            server.setPromptCount(0);
            server.setResourceCount(0);
            server.setContextTokenCount(0);
            server.setStatusMessage("未启用");
            return;
        }
        McpServerStatus status = serverStatuses.get(server.getId());
        if (status == null) {
            server.setConnected(false);
            server.setToolCount(0);
            server.setPromptCount(0);
            server.setResourceCount(0);
            server.setContextTokenCount(0);
            server.setStatusMessage("未加载");
            return;
        }
        server.setConnected(status.connected());
        server.setToolCount(status.toolCount());
        server.setPromptCount(status.promptCount());
        server.setResourceCount(status.resourceCount());
        server.setContextTokenCount(status.contextTokenCount());
        server.setStatusMessage(status.message());
    }

    public AiMcpServerInspectDto inspect(String serverId) {
        AiMcpServer server = mcpServerService.getByIdOrThrow(serverId);
        applyStatus(server);
        McpSyncClient client = activeClientMap.get(serverId);
        if (client == null) {
            ManualStreamableServer manualServer = manualStreamableServers.get(serverId);
            if (manualServer != null) {
                McpServerSnapshot snapshot = manualServer.snapshot();
                return new AiMcpServerInspectDto(
                        server,
                        new AiMcpServerInspectDto.RuntimeInfo(
                                true,
                                "已连接",
                                snapshot.toolCount(),
                                snapshot.promptCount(),
                                snapshot.resourceCount(),
                                snapshot.contextTokenCount(),
                                snapshot.instructions(),
                                toMap(snapshot.serverInfo()),
                                toMap(snapshot.capabilities())),
                        snapshot.tools(),
                        snapshot.prompts(),
                        snapshot.resources());
            }
            return new AiMcpServerInspectDto(
                    server,
                    new AiMcpServerInspectDto.RuntimeInfo(
                            server.getConnected(),
                            server.getStatusMessage(),
                            server.getToolCount(),
                            server.getPromptCount(),
                            server.getResourceCount(),
                            server.getContextTokenCount(),
                            null,
                            Map.of(),
                            Map.of()),
                    List.of(),
                    List.of(),
                    List.of());
        }
        McpServerSnapshot snapshot = inspectClient(client);
        updateStatus(serverId, McpServerStatus.connected(snapshot));
        applyStatus(server);
        return new AiMcpServerInspectDto(
                server,
                new AiMcpServerInspectDto.RuntimeInfo(
                        true,
                        "已连接",
                        snapshot.toolCount(),
                        snapshot.promptCount(),
                        snapshot.resourceCount(),
                        snapshot.contextTokenCount(),
                        snapshot.instructions(),
                        toMap(snapshot.serverInfo()),
                        toMap(snapshot.capabilities())),
                snapshot.tools(),
                snapshot.prompts(),
                snapshot.resources());
    }

    /**
     * 返回所有已连接 MCP 服务器提供的工具回调列表。
     */
    public List<ToolCallback> listToolCallbacks() {
        return listToolCallbacks((Consumer<AiChatSseEvent>) null);
    }

    /**
     * 返回所有已连接 MCP 服务器提供的工具回调列表，并发送前端过程事件。
     */
    public List<ToolCallback> listToolCallbacks(Consumer<AiChatSseEvent> eventSink) {
        List<McpSyncClient> clients = activeClients;
        List<ToolCallback> manualCallbacks = listManualStreamableCallbacks(eventSink);
        if (clients.isEmpty()) {
            List<ToolCallback> callbacks = reloadAndListToolCallbacks(eventSink, "当前没有可用 MCP 客户端");
            return mergeCallbacks(callbacks, manualCallbacks);
        }
        List<ToolCallback> callbacks = listToolCallbacks(clients, eventSink);
        if (callbacks.isEmpty() && hasEnabledServers()) {
            callbacks = reloadAndListToolCallbacks(eventSink, "MCP 工具列表为空");
        }
        return mergeCallbacks(callbacks, manualCallbacks);
    }

    /**
     * 只返回指定 MCP 服务提供的工具回调。连接仍由 Manager 统一维护，
     * 但每次聊天可以按前端选择缩小挂载范围。
     */
    public List<ToolCallback> listToolCallbacks(Collection<String> serverIds, Consumer<AiChatSseEvent> eventSink) {
        Set<String> selectedIds = normalizeServerIds(serverIds);
        if (selectedIds.isEmpty()) {
            return List.of();
        }

        List<ToolCallback> callbacks = listSelectedToolCallbacks(selectedIds, eventSink);
        if (callbacks.isEmpty() && hasEnabledServers()) {
            try {
                reload();
                callbacks = listSelectedToolCallbacks(selectedIds, eventSink);
            } catch (Exception e) {
                log.warn("Reload MCP before listing selected tool callbacks failed: {}", e.getMessage());
            }
        }
        return callbacks;
    }

    private List<ToolCallback> reloadAndListToolCallbacks(Consumer<AiChatSseEvent> eventSink, String reason) {
        if (!hasEnabledServers()) {
            return List.of();
        }
        log.info("{}，自动重载 MCP 后重试工具挂载", reason);
        try {
            reload();
            return listToolCallbacks(activeClients, eventSink);
        } catch (Exception e) {
            log.warn("Reload MCP before listing tool callbacks failed: {}", e.getMessage());
            return List.of();
        }
    }

    private boolean hasEnabledServers() {
        try {
            return !mcpServerService.listEnabled().isEmpty();
        } catch (Exception e) {
            log.debug("Failed to check enabled MCP servers", e);
            return false;
        }
    }

    private List<ToolCallback> listToolCallbacks(List<McpSyncClient> clients, Consumer<AiChatSseEvent> eventSink) {
        if (clients.isEmpty()) {
            return List.of();
        }
        try {
            ToolCallback[] callbacks = new SyncMcpToolCallbackProvider(clients).getToolCallbacks();
            return Arrays.stream(callbacks)
                    .map(callback -> wrapMcpCallback(callback, eventSink))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to list MCP tool callbacks: {}", e.getMessage());
            return List.of();
        }
    }

    private List<ToolCallback> listManualStreamableCallbacks(Consumer<AiChatSseEvent> eventSink) {
        return manualStreamableServers.values().stream()
                .flatMap(server -> server.snapshot().tools().stream()
                        .map(tool -> manualStreamableCallback(server.server(), tool, eventSink)))
                .toList();
    }

    private List<ToolCallback> listSelectedToolCallbacks(Set<String> selectedIds, Consumer<AiChatSseEvent> eventSink) {
        List<McpSyncClient> clients = selectedIds.stream()
                .map(activeClientMap::get)
                .filter(client -> client != null)
                .toList();
        List<ToolCallback> sdkCallbacks = listToolCallbacks(clients, eventSink);
        List<ToolCallback> manualCallbacks = manualStreamableServers.entrySet().stream()
                .filter(entry -> selectedIds.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().snapshot().tools().stream()
                        .map(tool -> manualStreamableCallback(entry.getValue().server(), tool, eventSink)))
                .toList();
        return mergeCallbacks(sdkCallbacks, manualCallbacks);
    }

    private Set<String> normalizeServerIds(Collection<String> serverIds) {
        if (serverIds == null || serverIds.isEmpty()) {
            return Set.of();
        }
        Set<String> selectedIds = new HashSet<>();
        for (String id : serverIds) {
            if (StringUtils.hasText(id)) {
                selectedIds.add(id);
            }
        }
        return selectedIds;
    }

    private List<ToolCallback> mergeCallbacks(List<ToolCallback> sdkCallbacks, List<ToolCallback> manualCallbacks) {
        if ((sdkCallbacks == null || sdkCallbacks.isEmpty()) && (manualCallbacks == null || manualCallbacks.isEmpty())) {
            return List.of();
        }
        List<ToolCallback> callbacks = new ArrayList<>();
        if (sdkCallbacks != null) {
            callbacks.addAll(sdkCallbacks);
        }
        if (manualCallbacks != null) {
            callbacks.addAll(manualCallbacks);
        }
        return callbacks;
    }

    @Override
    public void destroy() {
        initExecutor.shutdownNow();
        closeAll();
    }

    private synchronized void closeAll() {
        for (McpSyncClient client : activeClients) {
            closeClient(client);
        }
        activeClients = List.of();
        activeClientMap = Map.of();
        manualStreamableServers = Map.of();
    }

    private void putClient(String serverId, McpSyncClient client) {
        Map<String, McpSyncClient> clients = new HashMap<>(activeClientMap);
        clients.put(serverId, client);
        activeClientMap = Collections.unmodifiableMap(clients);
        activeClients = List.copyOf(clients.values());
    }

    private void removeClient(String serverId) {
        Map<String, McpSyncClient> clients = new HashMap<>(activeClientMap);
        McpSyncClient oldClient = clients.remove(serverId);
        closeClient(oldClient);
        activeClientMap = Collections.unmodifiableMap(clients);
        activeClients = List.copyOf(clients.values());
    }

    private void putManualStreamableServer(String serverId, ManualStreamableServer server) {
        Map<String, ManualStreamableServer> servers = new HashMap<>(manualStreamableServers);
        servers.put(serverId, server);
        manualStreamableServers = Collections.unmodifiableMap(servers);
    }

    private void removeManualStreamableServer(String serverId) {
        Map<String, ManualStreamableServer> servers = new HashMap<>(manualStreamableServers);
        servers.remove(serverId);
        manualStreamableServers = Collections.unmodifiableMap(servers);
    }

    private void updateStatus(String serverId, McpServerStatus status) {
        Map<String, McpServerStatus> statuses = new HashMap<>(serverStatuses);
        statuses.put(serverId, status);
        serverStatuses = Collections.unmodifiableMap(statuses);
    }

    private void closeClient(McpSyncClient client) {
        if (client == null) {
            return;
        }
        try {
            client.close();
        } catch (Exception e) {
            log.debug("Error closing MCP client", e);
        }
    }

    private McpSyncClient buildClient(AiMcpServer server) {
        McpSyncClient client = McpClient.sync(createTransport(server))
                .requestTimeout(Duration.ofSeconds(30))
                .build();
        client.initialize();
        return client;
    }

    private ManualStreamableServer buildManualStreamableServer(AiMcpServer server) {
        JsonNode initialize = postJsonRpc(server, Map.of(
                "jsonrpc", "2.0",
                "id", 1,
                "method", "initialize",
                "params", Map.of(
                        "protocolVersion", "2025-06-18",
                        "capabilities", Map.of(),
                        "clientInfo", Map.of("name", "fast-admin", "version", "0.0.1"))));
        JsonNode result = initialize.path("result");
        JsonNode toolsResult = postJsonRpc(server, Map.of(
                "jsonrpc", "2.0",
                "id", 2,
                "method", "tools/list",
                "params", Map.of())).path("result");
        List<AiMcpServerInspectDto.ToolInfo> tools = new ArrayList<>();
        for (JsonNode tool : toolsResult.path("tools")) {
            tools.add(new AiMcpServerInspectDto.ToolInfo(
                    tool.path("name").asText(),
                    textOrNull(tool.path("title")),
                    textOrNull(tool.path("description")),
                    objectMapper.convertValue(tool.path("inputSchema"),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                            }),
                    Map.of()));
        }
        McpSchema.Implementation serverInfo = objectMapper.convertValue(result.path("serverInfo"),
                McpSchema.Implementation.class);
        McpSchema.ServerCapabilities capabilities = objectMapper.convertValue(result.path("capabilities"),
                McpSchema.ServerCapabilities.class);
        McpServerSnapshot snapshot = new McpServerSnapshot(null, serverInfo, capabilities, tools, List.of(), List.of(),
                estimateContextTokens(null, tools, List.of(), List.of()));
        return new ManualStreamableServer(server, snapshot);
    }

    private ToolCallback manualStreamableCallback(AiMcpServer server, AiMcpServerInspectDto.ToolInfo tool,
            Consumer<AiChatSseEvent> eventSink) {
        String schema = toJson(tool.inputSchema());
        return FunctionToolCallback
                .builder(tool.name(), (Map<String, Object> input, ToolContext context) -> {
                    String argsJson = toJson(input);
                    notify(eventSink, AiChatSseEvent.toolStart(tool.name(), "mcp", argsJson));
                    long start = System.currentTimeMillis();
                    try {
                        JsonNode response = postJsonRpc(server, Map.of(
                                "jsonrpc", "2.0",
                                "id", System.currentTimeMillis(),
                                "method", "tools/call",
                                "params", Map.of(
                                        "name", tool.name(),
                                        "arguments", input == null ? Map.of() : input)));
                        String result = response.path("result").isMissingNode()
                                ? response.toString()
                                : response.path("result").toString();
                        notify(eventSink, AiChatSseEvent.toolEnd(tool.name(), "mcp", true,
                                System.currentTimeMillis() - start));
                        return result;
                    } catch (Exception e) {
                        notify(eventSink, AiChatSseEvent.toolEnd(tool.name(), "mcp", false,
                                System.currentTimeMillis() - start));
                        throw e;
                    }
                })
                .description(StringUtils.hasText(tool.description()) ? tool.description() : tool.name())
                .inputSchema(StringUtils.hasText(schema) ? schema : "{\"type\":\"object\"}")
                .inputType(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .build();
    }

    private JsonNode postJsonRpc(AiMcpServer server, Map<String, Object> payload) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(server.getUrl()))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json, text/event-stream")
                    .header("MCP-Protocol-Version", "2025-06-18")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)));
            parseJsonObject(server.getHeadersJson()).forEach(builder::header);
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BizException("MCP HTTP 请求失败：" + response.statusCode() + " " + response.body());
            }
            if (!StringUtils.hasText(response.body())) {
                return objectMapper.createObjectNode();
            }
            JsonNode json = objectMapper.readTree(response.body());
            if (json.has("error")) {
                throw new BizException("MCP 返回错误：" + json.path("error").toString());
            }
            return json;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("MCP HTTP 请求失败：" + e.getMessage());
        }
    }

    private McpServerSnapshot inspectClient(McpSyncClient client) {
        String instructions = safeGet("server instructions", () -> client.getServerInstructions());
        McpSchema.Implementation serverInfo = safeGet("server info", () -> client.getServerInfo());
        McpSchema.ServerCapabilities capabilities = safeGet("server capabilities", () -> client.getServerCapabilities());
        List<AiMcpServerInspectDto.ToolInfo> tools = safeList("MCP tools", () -> client.listTools().tools())
                .stream()
                .map(tool -> new AiMcpServerInspectDto.ToolInfo(
                        tool.name(),
                        tool.title(),
                        tool.description(),
                        nullToEmpty(tool.inputSchema()),
                        nullToEmpty(tool.outputSchema())))
                .toList();
        List<AiMcpServerInspectDto.PromptInfo> prompts = safeList("MCP prompts", () -> client.listPrompts().prompts())
                .stream()
                .map(prompt -> new AiMcpServerInspectDto.PromptInfo(
                        prompt.name(),
                        prompt.title(),
                        prompt.description(),
                        safeList("MCP prompt arguments", prompt::arguments)
                                .stream()
                                .map(argument -> new AiMcpServerInspectDto.PromptArgumentInfo(
                                        argument.name(),
                                        argument.description(),
                                        argument.required()))
                                .toList()))
                .toList();
        List<AiMcpServerInspectDto.ResourceInfo> resources = safeList("MCP resources",
                () -> client.listResources().resources())
                .stream()
                .map(resource -> new AiMcpServerInspectDto.ResourceInfo(
                        resource.uri(),
                        resource.name(),
                        resource.title(),
                        resource.description(),
                        resource.mimeType(),
                        resource.size()))
                .toList();
        int contextTokenCount = estimateContextTokens(instructions, tools, prompts, resources);
        return new McpServerSnapshot(instructions, serverInfo, capabilities, tools, prompts, resources,
                contextTokenCount);
    }

    private <T> T safeGet(String label, SupplierWithException<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.debug("Failed to read {}: {}", label, e.getMessage());
            return null;
        }
    }

    private <T> List<T> safeList(String label, SupplierWithException<List<T>> supplier) {
        try {
            List<T> value = supplier.get();
            return value == null ? List.of() : value;
        } catch (Exception e) {
            log.debug("Failed to list {}: {}", label, e.getMessage());
            return List.of();
        }
    }

    private Map<String, Object> toMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        return objectMapper.convertValue(value, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
        });
    }

    private Map<String, Object> nullToEmpty(Map<String, Object> value) {
        return value == null ? Map.of() : value;
    }

    private String textOrNull(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private int estimateContextTokens(String instructions, List<AiMcpServerInspectDto.ToolInfo> tools,
            List<AiMcpServerInspectDto.PromptInfo> prompts, List<AiMcpServerInspectDto.ResourceInfo> resources) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "instructions", instructions == null ? "" : instructions,
                    "tools", tools,
                    "prompts", prompts,
                    "resources", resources));
            return Math.max(1, (int) Math.ceil(payload.length() / 4.0));
        } catch (Exception e) {
            return 0;
        }
    }

    private McpClientTransport createTransport(AiMcpServer server) {
        return switch (server.getTransport()) {
            case "stdio" -> createStdioTransport(server);
            case "sse" -> createSseTransport(server);
            case "streamable-http" -> createStreamableHttpTransport(server);
            default -> throw new BizException("不支持的 MCP 传输类型: " + server.getTransport());
        };
    }

    private ToolCallback wrapMcpCallback(ToolCallback delegate, Consumer<AiChatSseEvent> eventSink) {
        if (eventSink == null) {
            return delegate;
        }
        return new ToolCallback() {
            @Override
            public ToolDefinition getToolDefinition() {
                return delegate.getToolDefinition();
            }

            @Override
            public ToolMetadata getToolMetadata() {
                return delegate.getToolMetadata();
            }

            @Override
            public String call(String toolInput) {
                return callMcp(delegate, toolInput, null, eventSink);
            }

            @Override
            public String call(String toolInput, ToolContext toolContext) {
                return callMcp(delegate, toolInput, toolContext, eventSink);
            }
        };
    }

    private String callMcp(ToolCallback delegate, String toolInput, ToolContext toolContext,
            Consumer<AiChatSseEvent> eventSink) {
        String toolName = delegate.getToolDefinition().name();
        notify(eventSink, AiChatSseEvent.toolStart(toolName, "mcp", toolInput));
        long start = System.currentTimeMillis();
        try {
            String result = toolContext == null ? delegate.call(toolInput) : delegate.call(toolInput, toolContext);
            notify(eventSink, AiChatSseEvent.toolEnd(toolName, "mcp", true, System.currentTimeMillis() - start));
            return result;
        } catch (Exception e) {
            notify(eventSink, AiChatSseEvent.toolEnd(toolName, "mcp", false, System.currentTimeMillis() - start));
            throw e;
        }
    }

    private void notify(Consumer<AiChatSseEvent> eventSink, AiChatSseEvent event) {
        try {
            eventSink.accept(event);
        } catch (Exception e) {
            log.debug("Failed to send MCP tool SSE event", e);
        }
    }

    private StdioClientTransport createStdioTransport(AiMcpServer server) {
        List<String> args = parseJsonArray(server.getArgsJson());
        ServerParameters params = ServerParameters.builder(server.getCommand())
                .args(args)
                .build();
        return new StdioClientTransport(params, McpJsonDefaults.getMapper());
    }

    private HttpClientSseClientTransport createSseTransport(AiMcpServer server) {
        URI uri = URI.create(server.getUrl());
        String baseUri = uri.getScheme() + "://" + uri.getRawAuthority();
        String endpoint = StringUtils.hasText(uri.getRawPath()) ? uri.getRawPath() : "/sse";
        if (StringUtils.hasText(uri.getRawQuery())) {
            endpoint += "?" + uri.getRawQuery();
        }
        HttpClientSseClientTransport.Builder builder = HttpClientSseClientTransport.builder(baseUri)
                .sseEndpoint(endpoint);
        applyHeaders(builder, server);
        return builder.build();
    }

    private HttpClientStreamableHttpTransport createStreamableHttpTransport(AiMcpServer server) {
        HttpClientStreamableHttpTransport.Builder builder = HttpClientStreamableHttpTransport.builder(server.getUrl());
        applyHeaders(builder, server);
        return builder.build();
    }

    private void applyHeaders(HttpClientSseClientTransport.Builder builder, AiMcpServer server) {
        Map<String, String> headers = parseJsonObject(server.getHeadersJson());
        if (!headers.isEmpty()) {
            java.net.http.HttpRequest.Builder rb = java.net.http.HttpRequest.newBuilder();
            headers.forEach(rb::header);
            builder.requestBuilder(rb);
        }
    }

    private void applyHeaders(HttpClientStreamableHttpTransport.Builder builder, AiMcpServer server) {
        Map<String, String> headers = parseJsonObject(server.getHeadersJson());
        if (!headers.isEmpty()) {
            java.net.http.HttpRequest.Builder rb = java.net.http.HttpRequest.newBuilder();
            headers.forEach(rb::header);
            builder.requestBuilder(rb);
        }
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

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }

    private record McpServerSnapshot(
            String instructions,
            McpSchema.Implementation serverInfo,
            McpSchema.ServerCapabilities capabilities,
            List<AiMcpServerInspectDto.ToolInfo> tools,
            List<AiMcpServerInspectDto.PromptInfo> prompts,
            List<AiMcpServerInspectDto.ResourceInfo> resources,
            int contextTokenCount) {

        int toolCount() {
            return tools.size();
        }

        int promptCount() {
            return prompts.size();
        }

        int resourceCount() {
            return resources.size();
        }
    }

    private record ManualStreamableServer(
            AiMcpServer server,
            McpServerSnapshot snapshot) {
    }

    private record McpServerStatus(
            boolean connected,
            int toolCount,
            int promptCount,
            int resourceCount,
            int contextTokenCount,
            String message) {

        static McpServerStatus connected(McpServerSnapshot snapshot) {
            return new McpServerStatus(true, snapshot.toolCount(), snapshot.promptCount(), snapshot.resourceCount(),
                    snapshot.contextTokenCount(), "已连接");
        }

        static McpServerStatus disconnected(String message) {
            return new McpServerStatus(false, 0, 0, 0, 0, message);
        }
    }
}
