package cc.oofo.ai.mcp.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.ai.mcp.dto.AiMcpServerInspectDto;
import cc.oofo.ai.mcp.dto.AiMcpServerSaveDto;
import cc.oofo.ai.mcp.entity.AiMcpServer;
import cc.oofo.ai.mcp.entity.query.AiMcpServerQuery;
import cc.oofo.ai.mcp.service.AiMcpClientManager;
import cc.oofo.ai.mcp.service.AiMcpServerService;
import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

/**
 * MCP 服务配置管理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/mcp/server")
public class AiMcpServerController {

    private final AiMcpServerService service;
    private final AiMcpClientManager mcpClientManager;

    @GetMapping
    public Ps<AiMcpServer> page(AiMcpServerQuery query) {
        Page<AiMcpServer> page = service.page(query);
        page.getRecords().forEach(mcpClientManager::applyStatus);
        return Ps.ok(page);
    }

    @GetMapping("/{id}")
    public Rs<AiMcpServer> detail(@PathVariable String id) {
        AiMcpServer server = service.getByIdOrThrow(id);
        mcpClientManager.applyStatus(server);
        return Rs.ok(server);
    }

    @GetMapping("/{id}/inspect")
    public Rs<AiMcpServerInspectDto> inspect(@PathVariable String id) {
        return Rs.ok(mcpClientManager.inspect(id));
    }

    @PostMapping
    @OperationLog(title = "MCP 服务配置", type = BusinessType.CREATE)
    public Rs<Void> add(@RequestBody AiMcpServerSaveDto dto) {
        AiMcpServer server = service.add(dto);
        mcpClientManager.reload(server.getId());
        return Rs.ok();
    }

    @PutMapping
    @OperationLog(title = "MCP 服务配置", type = BusinessType.UPDATE)
    public Rs<Void> update(@RequestBody AiMcpServerSaveDto dto) {
        service.update(dto);
        mcpClientManager.reload(dto.getId());
        return Rs.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(title = "MCP 服务配置", type = BusinessType.DELETE)
    public Rs<Void> del(@PathVariable String id) {
        service.del(id);
        mcpClientManager.remove(id);
        return Rs.ok();
    }

    @PostMapping("/{id}/enabled")
    @OperationLog(title = "MCP 服务配置", type = BusinessType.UPDATE)
    public Rs<Void> changeEnabled(@PathVariable String id, @RequestParam boolean enabled) {
        service.changeEnabled(id, enabled);
        mcpClientManager.reload(id);
        return Rs.ok();
    }

    /**
     * 只重新加载单个 MCP 服务连接。
     */
    @PostMapping("/{id}/reload")
    @OperationLog(title = "MCP 服务配置", type = BusinessType.UPDATE)
    public Rs<Void> reloadOne(@PathVariable String id) {
        mcpClientManager.reload(id);
        return Rs.ok();
    }
}
