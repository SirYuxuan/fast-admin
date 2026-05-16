package cc.oofo.system.file.controller;

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
import cc.oofo.system.file.entity.dto.SysFileConfigDto;
import cc.oofo.system.file.entity.dto.SysFileConfigSaveDto;
import cc.oofo.system.file.entity.query.SysFileConfigQuery;
import cc.oofo.system.file.service.SysFileConfigService;
import lombok.RequiredArgsConstructor;

/**
 * 文件存储配置控制器
 *
 * @author Sir丶雨轩
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/system/file/config")
public class SysFileConfigController {

    private final SysFileConfigService service;

    @GetMapping
    public Ps<SysFileConfigDto> page(SysFileConfigQuery query) {
        return Ps.ok(service.page(query));
    }

    @GetMapping(path = "/{id}")
    public Rs<SysFileConfigDto> detail(@PathVariable String id) {
        return Rs.ok(service.detail(id));
    }

    @PostMapping
    public Rs<Void> add(@RequestBody SysFileConfigSaveDto dto) {
        service.add(dto);
        return Rs.ok();
    }

    @PutMapping
    public Rs<Void> update(@RequestBody SysFileConfigSaveDto dto) {
        service.update(dto);
        return Rs.ok();
    }

    @PostMapping(path = "/{id}/activate")
    public Rs<Void> activate(@PathVariable String id) {
        service.activate(id);
        return Rs.ok();
    }

    @DeleteMapping(path = "/{id}")
    public Rs<Void> del(@PathVariable String id) {
        service.del(id);
        return Rs.ok();
    }

}
