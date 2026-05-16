package cc.oofo.system.config.controller;

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
import cc.oofo.system.config.entity.SysConfig;
import cc.oofo.system.config.entity.query.SysConfigQuery;
import cc.oofo.system.config.service.SysConfigService;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/system/config")
public class SysConfigController {

    private final SysConfigService service;

    @GetMapping
    public Ps<SysConfig> page(SysConfigQuery query) {
        return Ps.ok(service.page(query));
    }

    @GetMapping("/{id}")
    public Rs<SysConfig> detail(@PathVariable String id) {
        return Rs.ok(service.getById(id));
    }

    @PostMapping
    @OperationLog(title = "系统参数", type = BusinessType.CREATE)
    public Rs<Void> add(@RequestBody SysConfig data) {
        service.add(data);
        return Rs.ok();
    }

    @PutMapping
    @OperationLog(title = "系统参数", type = BusinessType.UPDATE)
    public Rs<Void> edit(@RequestBody SysConfig data) {
        service.edit(data);
        return Rs.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(title = "系统参数", type = BusinessType.DELETE)
    public Rs<Void> del(@PathVariable String id) {
        service.del(id);
        return Rs.ok();
    }
}
