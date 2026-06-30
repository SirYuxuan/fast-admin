package cc.oofo.flow.cc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.flow.cc.entity.FlowCcRecord;
import cc.oofo.flow.cc.service.FlowCcService;
import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import lombok.RequiredArgsConstructor;

/**
 * 抄送（抄送我的）接口。
 *
 * @author Sir丶雨轩
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/flow/cc")
public class FlowCcController {

    private final FlowCcService service;

    @GetMapping
    public Ps<FlowCcRecord> myCc(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<FlowCcRecord> result = service.myCc(page, pageSize);
        return Ps.ok(result);
    }

    @GetMapping("/unread")
    public Rs<Long> unread() {
        return Rs.ok(service.unread());
    }

    @PostMapping("/{id}/read")
    public Rs<Void> markRead(@PathVariable String id) {
        service.markRead(id);
        return Rs.ok();
    }
}
