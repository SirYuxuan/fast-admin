package cc.oofo.system.log.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.log.entity.SysOperationLog;
import cc.oofo.system.log.entity.query.SysOperationLogQuery;
import cc.oofo.system.log.service.SysOperationLogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/system/log/operation")
public class SysOperationLogController {

    private final SysOperationLogService service;

    @GetMapping
    public Ps<SysOperationLog> page(SysOperationLogQuery query) {
        return Ps.ok(service.page(query));
    }

    @DeleteMapping("/{id}")
    public Rs<Void> del(@PathVariable String id) {
        service.removeById(id);
        return Rs.ok();
    }

    @DeleteMapping("/clean")
    public Rs<Void> clean() {
        service.clean();
        return Rs.ok();
    }

    @GetMapping("/{id}")
    public Rs<SysOperationLog> detail(@PathVariable String id) {
        return Rs.ok(service.getById(id));
    }
}
