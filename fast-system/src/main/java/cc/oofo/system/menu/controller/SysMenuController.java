package cc.oofo.system.menu.controller;

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

import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.menu.entity.dto.SysMenuDto;
import cc.oofo.system.menu.entity.dto.SysMenuSaveDto;
import cc.oofo.system.menu.service.SysMenuService;
import lombok.RequiredArgsConstructor;

/**
 * 系统菜单控制器
 * 
 * @author Sir丶雨轩
 * @since 2025/11/13
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/system/menu")
public class SysMenuController {

    private final SysMenuService menuService;

    /**
     * 获取当前用户的菜单树
     * 
     * @return 菜单树结果
     */
    @GetMapping(path = "/userMenu")
    public Rs<List<SysMenuDto>> userMenu() {
        return Rs.ok(menuService.userMenu());
    }

    /**
     * 检查菜单名称是否存在
     * 
     * @param id   菜单ID（可选，编辑时传入）
     * @param name 菜单名称
     * @return 是否存在
     */
    @GetMapping(path = "/nameExists")
    public Rs<Boolean> nameExists(@RequestParam(required = false) String id, @RequestParam String name) {
        return Rs.ok(menuService.nameExists(id, name));
    }

    /**
     * 检查菜单路径是否存在
     * 
     * @param id   菜单ID（可选，编辑时传入）
     * @param path 菜单路径
     * @return 是否存在
     */
    @GetMapping(path = "/pathExists")
    public Rs<Boolean> pathExists(@RequestParam(required = false) String id, @RequestParam String path) {
        return Rs.ok(menuService.pathExists(id, path));
    }

    /**
     * 删除菜单
     * 
     * @param id 菜单ID
     * @return 删除结果
     */
    @DeleteMapping(path = "/{id}")
    public Rs<Void> del(@PathVariable String id) {
        menuService.del(id);
        return Rs.ok();
    }

    /**
     * 添加菜单
     * 
     * @param menuSaveDto 菜单保存DTO
     * @return 操作结果
     */
    @PostMapping
    public Rs<Void> add(@RequestBody SysMenuSaveDto menuSaveDto) {
        menuService.add(menuSaveDto);
        return Rs.ok();
    }

    /**
     * 更新菜单
     * 
     * @param menuSaveDto 菜单保存DTO
     * @return 操作结果
     */
    @PutMapping
    public Rs<Void> update(@RequestBody SysMenuSaveDto menuSaveDto) {
        menuService.update(menuSaveDto);
        return Rs.ok();
    }

    /**
     * 获取所有菜单树
     * 
     * @return 菜单树结果
     */
    @GetMapping
    public Rs<List<SysMenuDto>> list() {
        return Rs.ok(menuService.buildMenuTree(menuService.list(), true));
    }

}
