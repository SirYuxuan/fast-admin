package cc.oofo.ai.tool.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.ai.tool.dto.AiToolConfigSaveDto;
import cc.oofo.ai.tool.entity.AiToolConfig;
import cc.oofo.ai.tool.entity.query.AiToolConfigQuery;
import cc.oofo.ai.tool.service.AiToolConfigService;
import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

/**
 * AI 工具配置管理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/tool")
public class AiToolConfigController {

    private final AiToolConfigService service;

    @GetMapping
    public Ps<AiToolConfig> page(AiToolConfigQuery query) {
        return Ps.ok(service.page(query));
    }

    @GetMapping("/{id}")
    public Rs<AiToolConfig> detail(@PathVariable String id) {
        return Rs.ok(service.getByIdOrThrow(id));
    }

    @PostMapping
    @OperationLog(title = "AI 工具配置", type = BusinessType.CREATE)
    public Rs<Void> add(@RequestBody AiToolConfigSaveDto dto) {
        service.add(dto);
        return Rs.ok();
    }

    @PutMapping
    @OperationLog(title = "AI 工具配置", type = BusinessType.UPDATE)
    public Rs<Void> update(@RequestBody AiToolConfigSaveDto dto) {
        service.update(dto);
        return Rs.ok();
    }

    @PostMapping("/{id}/enabled")
    @OperationLog(title = "AI 工具配置", type = BusinessType.UPDATE)
    public Rs<Void> changeEnabled(@PathVariable String id, @RequestParam boolean enabled) {
        service.changeEnabled(id, enabled);
        return Rs.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(title = "AI 工具配置", type = BusinessType.DELETE)
    public Rs<Void> del(@PathVariable String id) {
        service.del(id);
        return Rs.ok();
    }
}
