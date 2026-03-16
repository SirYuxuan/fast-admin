package cc.oofo.system.dept.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.dept.entity.SysDept;
import cc.oofo.system.dept.entity.dto.SysDeptDto;
import cc.oofo.system.dept.entity.dto.SysDeptSaveDto;
import cc.oofo.system.dept.service.SysDeptService;
import lombok.RequiredArgsConstructor;

/**
 * 系统部门控制器
 * 
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/system/dept")
public class SysDeptController {

    private final SysDeptService deptService;

    /**
     * 获取部门列表
     * 
     * @return 部门列表结果
     */
    @GetMapping
    public Rs<List<SysDeptDto>> list(BaseQuery<SysDept> query) {
        return Ps.ok(deptService.list(query));
    }

    /**
     * 检查部门名称是否存在
     * 
     * @param id   部门ID（可选，编辑时传入）
     * @param name 部门名称
     * @return 是否存在
     */
    @GetMapping(path = "/nameExists")
    public Rs<Boolean> nameExists(@RequestParam(required = false) String id, @RequestParam String name) {
        return Rs.ok(deptService.nameExists(id, name));
    }

    /**
     * 获取所有部门（用于下拉选择）
     * 
     * @return 部门列表
     */
    @GetMapping(path = "/all")
    public Rs<List<SysDept>> selectAll() {
        return Rs.ok(deptService.selectAll());
    }

    /**
     * 添加部门
     * 
     * @param deptSaveDto 部门保存DTO
     * @return 操作结果
     */
    @PostMapping
    public Rs<Void> add(@RequestBody SysDeptSaveDto deptSaveDto) {
        deptService.add(deptSaveDto);
        return Rs.ok();
    }

    /**
     * 更新部门
     * 
     * @param id          部门ID
     * @param deptSaveDto 部门保存DTO
     * @return 操作结果
     */
    @PutMapping
    public Rs<Void> update(@RequestBody SysDeptSaveDto deptSaveDto) {
        deptService.update(deptSaveDto);
        return Rs.ok();
    }

    /**
     * 删除部门
     * 
     * @param id 部门ID
     * @return 删除结果
     */
    @DeleteMapping(path = "/{id}")
    public Rs<Void> del(@PathVariable String id) {
        deptService.del(id);
        return Rs.ok();
    }

}
