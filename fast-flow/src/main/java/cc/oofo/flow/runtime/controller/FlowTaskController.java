package cc.oofo.flow.runtime.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.flow.runtime.dto.CompleteTaskDto;
import cc.oofo.flow.runtime.service.FlowTaskService;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

/**
 * 待办任务接口。
 *
 * @author Sir丶雨轩
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/flow/task")
public class FlowTaskController {

    private final FlowTaskService service;

    @GetMapping("/todo")
    public Rs<Map<String, Object>> todo(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Rs.ok(service.todo(page, pageSize));
    }

    @GetMapping("/done")
    public Rs<Map<String, Object>> done(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Rs.ok(service.done(page, pageSize));
    }

    @GetMapping("/{taskId}")
    public Rs<Map<String, Object>> detail(@PathVariable String taskId) {
        return Rs.ok(service.taskDetail(taskId));
    }

    @PostMapping("/{taskId}/complete")
    @OperationLog(title = "审批任务", type = BusinessType.UPDATE)
    public Rs<Void> complete(@PathVariable String taskId, @RequestBody CompleteTaskDto dto) {
        service.complete(taskId, dto.getOutcome(), dto.getComment(),
                dto.getVariables(), dto.getCcUserIds());
        return Rs.ok();
    }

    @PostMapping("/{taskId}/transfer")
    @OperationLog(title = "转办任务", type = BusinessType.UPDATE)
    public Rs<Void> transfer(@PathVariable String taskId,
            @RequestParam String toUserId,
            @RequestParam(required = false) String comment) {
        service.transfer(taskId, toUserId, comment);
        return Rs.ok();
    }

    @PostMapping("/{taskId}/claim")
    @OperationLog(title = "签收任务", type = BusinessType.UPDATE)
    public Rs<Void> claim(@PathVariable String taskId) {
        service.claim(taskId);
        return Rs.ok();
    }
}
