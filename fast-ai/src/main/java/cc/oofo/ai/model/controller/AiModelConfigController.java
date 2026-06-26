package cc.oofo.ai.model.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import cc.oofo.ai.model.dto.AiModelConfigDto;
import cc.oofo.ai.model.dto.AiModelConfigSaveDto;
import cc.oofo.ai.model.dto.AiModelTestResultDto;
import cc.oofo.ai.model.entity.query.AiModelConfigQuery;
import cc.oofo.ai.model.service.AiModelConfigService;
import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

/**
 * AI 模型配置管理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/model")
public class AiModelConfigController {

    private final AiModelConfigService service;

    @GetMapping
    public Ps<AiModelConfigDto> page(AiModelConfigQuery query) {
        return Ps.ok(service.page(query));
    }

    @GetMapping("/{id}")
    public Rs<AiModelConfigDto> detail(@PathVariable String id) {
        return Rs.ok(service.detail(id));
    }

    @PostMapping
    @OperationLog(title = "AI 模型配置", type = BusinessType.CREATE)
    public Rs<Void> add(@RequestBody AiModelConfigSaveDto dto) {
        service.add(dto);
        return Rs.ok();
    }

    @PutMapping
    @OperationLog(title = "AI 模型配置", type = BusinessType.UPDATE)
    public Rs<Void> update(@RequestBody AiModelConfigSaveDto dto) {
        service.update(dto);
        return Rs.ok();
    }

    @PostMapping("/fetch-models")
    public Rs<List<String>> fetchModels(@RequestBody AiModelConfigSaveDto dto) {
        return Rs.ok(service.fetchModels(dto));
    }

    @PostMapping("/test")
    @OperationLog(title = "AI 模型配置", type = BusinessType.OTHER)
    public Rs<AiModelTestResultDto> test(@RequestBody AiModelConfigSaveDto dto) {
        return Rs.ok(service.test(dto));
    }

    @PostMapping("/{id}/activate")
    @OperationLog(title = "AI 模型配置", type = BusinessType.UPDATE)
    public Rs<Void> activate(@PathVariable String id) {
        service.activate(id);
        return Rs.ok();
    }

    @PostMapping("/{id}/enabled")
    @OperationLog(title = "AI 模型配置", type = BusinessType.UPDATE)
    public Rs<Void> changeEnabled(@PathVariable String id, @RequestParam boolean enabled) {
        service.changeEnabled(id, enabled);
        return Rs.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(title = "AI 模型配置", type = BusinessType.DELETE)
    public Rs<Void> del(@PathVariable String id) {
        service.del(id);
        return Rs.ok();
    }
}
