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
import cc.oofo.system.dict.entity.SysDictData;
import cc.oofo.system.dict.entity.query.SysDictDataQuery;
import cc.oofo.system.dict.service.SysDictDataService;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dict/data")
public class SysDictDataController {

    private final SysDictDataService service;

    @GetMapping
    public Ps<SysDictData> page(SysDictDataQuery query) {
        return Ps.ok(service.page(query));
    }

    /** 根据字典类型取启用项（前端下拉用） */
    @GetMapping("/type/{dictType}")
    public Rs<List<SysDictData>> listByType(@PathVariable String dictType) {
        return Rs.ok(service.listByType(dictType));
    }

    @PostMapping
    @OperationLog(title = "字典数据", type = BusinessType.CREATE)
    public Rs<Void> add(@RequestBody SysDictData data) {
        service.add(data);
        return Rs.ok();
    }

    @PutMapping
    @OperationLog(title = "字典数据", type = BusinessType.UPDATE)
    public Rs<Void> edit(@RequestBody SysDictData data) {
        service.edit(data);
        return Rs.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(title = "字典数据", type = BusinessType.DELETE)
    public Rs<Void> del(@PathVariable String id) {
        service.removeById(id);
        return Rs.ok();
    }
}
