package cc.oofo.flow.runtime.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.flow.runtime.service.FlowProcessService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

/**
 * 流程发起与实例接口。
 *
 * @author Sir丶雨轩
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/flow/process")
public class FlowProcessController {

    private final FlowProcessService service;

    /** 发起流程：body = {processKey, variables} */
    @SuppressWarnings("unchecked")
    @PostMapping("/start")
    @OperationLog(title = "发起流程", type = BusinessType.CREATE)
    public Rs<String> start(@RequestBody Map<String, Object> body) {
        Object key = body.get("processKey");
        if (key == null) {
            throw new BizException("processKey 不能为空");
        }
        Map<String, Object> variables = (Map<String, Object>) body.get("variables");
        Rs<String> rs = Rs.ok();
        rs.setData(service.start(key.toString(), variables));
        return rs;
    }

    @GetMapping("/initiated")
    public Rs<Map<String, Object>> myInitiated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Rs.ok(service.myInitiated(page, pageSize));
    }

    @GetMapping("/{instanceId}")
    public Rs<Map<String, Object>> detail(@PathVariable String instanceId) {
        return Rs.ok(service.detail(instanceId));
    }

    @PostMapping("/{instanceId}/cancel")
    @OperationLog(title = "撤销流程", type = BusinessType.UPDATE)
    public Rs<Void> cancel(@PathVariable String instanceId,
            @RequestParam(required = false) String reason) {
        service.cancel(instanceId, reason);
        return Rs.ok();
    }
}
