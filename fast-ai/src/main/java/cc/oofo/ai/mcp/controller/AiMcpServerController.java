package cc.oofo.ai.mcp.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.ai.mcp.dto.AiMcpServerSaveDto;
import cc.oofo.ai.mcp.entity.AiMcpServer;
import cc.oofo.ai.mcp.entity.query.AiMcpServerQuery;
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

    @GetMapping
    public Ps<AiMcpServer> page(AiMcpServerQuery query) {
        return Ps.ok(service.page(query));
    }

    @GetMapping("/{id}")
    public Rs<AiMcpServer> detail(@PathVariable String id) {
        return Rs.ok(service.getByIdOrThrow(id));
    }

    @PostMapping
    @OperationLog(title = "MCP 服务配置", type = BusinessType.CREATE)
    public Rs<Void> add(@RequestBody AiMcpServerSaveDto dto) {
        service.add(dto);
        return Rs.ok();
    }

    @PutMapping
    @OperationLog(title = "MCP 服务配置", type = BusinessType.UPDATE)
    public Rs<Void> update(@RequestBody AiMcpServerSaveDto dto) {
        service.update(dto);
        return Rs.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(title = "MCP 服务配置", type = BusinessType.DELETE)
    public Rs<Void> del(@PathVariable String id) {
        service.del(id);
        return Rs.ok();
    }
}
