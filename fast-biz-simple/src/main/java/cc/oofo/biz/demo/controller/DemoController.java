package cc.oofo.biz.demo.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.biz.demo.entity.Demo;
import cc.oofo.biz.demo.entity.dto.DemoSaveDto;
import cc.oofo.biz.demo.entity.query.DemoQuery;
import cc.oofo.biz.demo.service.DemoService;
import cc.oofo.framework.web.response.Rs;
import lombok.RequiredArgsConstructor;

/**
 * 示例 Controller
 *
 * @author Sir丶雨轩
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/biz/demo")
public class DemoController {

    private final DemoService demoService;

    @GetMapping
    public Rs<Page<Demo>> page(DemoQuery query) {
        return Rs.ok(demoService.page(query));
    }

    @PostMapping
    public Rs<Void> add(@RequestBody DemoSaveDto dto) {
        demoService.add(dto);
        return Rs.ok();
    }

    @PutMapping
    public Rs<Void> update(@RequestBody DemoSaveDto dto) {
        demoService.update(dto);
        return Rs.ok();
    }

    @DeleteMapping(path = "/{id}")
    public Rs<Void> del(@PathVariable String id) {
        demoService.del(id);
        return Rs.ok();
    }

}
