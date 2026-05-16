package cc.oofo.system.dict.controller;

import java.util.List;

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
import cc.oofo.system.dict.entity.SysDictType;
import cc.oofo.system.dict.entity.query.SysDictTypeQuery;
import cc.oofo.system.dict.service.SysDictTypeService;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dict/type")
public class SysDictTypeController {

    private final SysDictTypeService service;

    @GetMapping
    public Ps<SysDictType> page(SysDictTypeQuery query) {
        return Ps.ok(service.page(query));
    }

    /** 全量列表（前端选项使用） */
    @GetMapping("/all")
    public Rs<List<SysDictType>> all() {
        return Rs.ok(service.query().eq("status", 1).list());
    }

    @PostMapping
    @OperationLog(title = "字典类型", type = BusinessType.CREATE)
    public Rs<Void> add(@RequestBody SysDictType data) {
        service.add(data);
        return Rs.ok();
    }

    @PutMapping
    @OperationLog(title = "字典类型", type = BusinessType.UPDATE)
    public Rs<Void> edit(@RequestBody SysDictType data) {
        service.edit(data);
        return Rs.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(title = "字典类型", type = BusinessType.DELETE)
    public Rs<Void> del(@PathVariable String id) {
        service.del(id);
        return Rs.ok();
    }
}
