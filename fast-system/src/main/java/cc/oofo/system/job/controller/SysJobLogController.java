package cc.oofo.system.job.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.job.entity.SysJobLog;
import cc.oofo.system.job.entity.query.SysJobLogQuery;
import cc.oofo.system.job.service.SysJobLogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/system/job/log")
public class SysJobLogController {

    private final SysJobLogService service;

    @GetMapping
    public Ps<SysJobLog> page(SysJobLogQuery query) {
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
}
