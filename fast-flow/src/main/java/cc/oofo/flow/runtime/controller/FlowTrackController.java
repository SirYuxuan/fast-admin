package cc.oofo.flow.runtime.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.flow.record.entity.FlowTaskRecord;
import cc.oofo.flow.runtime.service.FlowTrackService;
import cc.oofo.framework.web.response.Rs;
import lombok.RequiredArgsConstructor;

/**
 * 流程跟踪接口（流程图高亮 + 审批轨迹）。
 *
 * @author Sir丶雨轩
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/flow/track")
public class FlowTrackController {

    private final FlowTrackService service;

    @GetMapping("/{instanceId}/diagram")
    public Rs<Map<String, Object>> diagram(@PathVariable String instanceId) {
        return Rs.ok(service.diagram(instanceId));
    }

    @GetMapping("/{instanceId}/records")
    public Rs<List<FlowTaskRecord>> records(@PathVariable String instanceId) {
        return Rs.ok(service.records(instanceId));
    }
}
