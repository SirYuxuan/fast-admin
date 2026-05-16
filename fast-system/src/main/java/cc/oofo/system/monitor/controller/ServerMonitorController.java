package cc.oofo.system.monitor.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.monitor.service.ServerMonitorService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/system/monitor")
public class ServerMonitorController {

    private final ServerMonitorService service;

    /** 获取服务器监控全量信息 */
    @GetMapping("/server")
    public Rs<Map<String, Object>> server() {
        return Rs.ok(service.summary());
    }
}
