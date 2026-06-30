package cc.oofo.flow.definition.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.flow.definition.service.FlowDefinitionService;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

/**
 * 流程定义管理接口（已部署到引擎的流程及其版本）。
 *
 * @author Sir丶雨轩
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/flow/definition")
public class FlowDefinitionController {

    private final FlowDefinitionService service;

    @GetMapping
    public Rs<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String key) {
        return Rs.ok(service.page(page, pageSize, name, key));
    }

    @GetMapping("/startable")
    public Rs<List<Map<String, Object>>> startable(
            @RequestParam(required = false) String name) {
        return Rs.ok(service.startable(name));
    }

    @GetMapping("/versions")
    public Rs<List<Map<String, Object>>> versions(@RequestParam String key) {
        return Rs.ok(service.versions(key));
    }

    @GetMapping("/{definitionId}/start-form")
    public Rs<Map<String, Object>> startForm(@PathVariable String definitionId) {
        return Rs.ok(service.startForm(definitionId));
    }

    @GetMapping("/{definitionId}/xml")
    public Rs<String> xml(@PathVariable String definitionId) {
        Rs<String> rs = Rs.ok();
        rs.setData(service.getBpmnXml(definitionId));
        return rs;
    }

    @PutMapping("/{definitionId}/suspend")
    @OperationLog(title = "流程定义-挂起", type = BusinessType.UPDATE)
    public Rs<Void> suspend(@PathVariable String definitionId) {
        service.suspend(definitionId);
        return Rs.ok();
    }

    @PutMapping("/{definitionId}/activate")
    @OperationLog(title = "流程定义-激活", type = BusinessType.UPDATE)
    public Rs<Void> activate(@PathVariable String definitionId) {
        service.activate(definitionId);
        return Rs.ok();
    }

    @DeleteMapping("/deployment/{deploymentId}")
    @OperationLog(title = "流程定义-删除部署", type = BusinessType.DELETE)
    public Rs<Void> delete(@PathVariable String deploymentId,
            @RequestParam(defaultValue = "false") boolean cascade) {
        service.delete(deploymentId, cascade);
        return Rs.ok();
    }
}
