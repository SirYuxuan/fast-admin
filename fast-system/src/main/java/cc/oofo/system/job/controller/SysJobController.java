package cc.oofo.system.job.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.job.entity.SysJob;
import cc.oofo.system.job.entity.query.SysJobQuery;
import cc.oofo.system.job.service.SysJobService;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/system/job")
public class SysJobController {

    private final SysJobService service;

    @GetMapping
    public Ps<SysJob> page(SysJobQuery query) {
        return Ps.ok(service.page(query));
    }

    @GetMapping("/{id}")
    public Rs<SysJob> detail(@PathVariable String id) {
        return Rs.ok(service.getById(id));
    }

    @PostMapping
    @OperationLog(title = "定时任务", type = BusinessType.CREATE)
    public Rs<Void> add(@RequestBody SysJob job) {
        service.add(job);
        return Rs.ok();
    }

    @PutMapping
    @OperationLog(title = "定时任务", type = BusinessType.UPDATE)
    public Rs<Void> edit(@RequestBody SysJob job) {
        service.edit(job);
        return Rs.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(title = "定时任务", type = BusinessType.DELETE)
    public Rs<Void> del(@PathVariable String id) {
        service.del(id);
        return Rs.ok();
    }

    /** 启动任务 */
    @PostMapping("/{id}/start")
    @OperationLog(title = "定时任务", type = BusinessType.UPDATE)
    public Rs<Void> start(@PathVariable String id) {
        service.changeStatus(id, 1);
        return Rs.ok();
    }

    /** 暂停任务 */
    @PostMapping("/{id}/pause")
    @OperationLog(title = "定时任务", type = BusinessType.UPDATE)
    public Rs<Void> pause(@PathVariable String id) {
        service.changeStatus(id, 0);
        return Rs.ok();
    }

    /** 立即执行一次 */
    @PostMapping("/{id}/run")
    @OperationLog(title = "定时任务", type = BusinessType.OTHER)
    public Rs<Void> run(@PathVariable String id) {
        service.runOnce(id);
        return Rs.ok();
    }
}
