package cc.oofo.flow.form.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.flow.form.entity.FlowForm;
import cc.oofo.flow.form.entity.query.FlowFormQuery;
import cc.oofo.flow.form.service.FlowFormService;
import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

/**
 * 自定义表单管理接口。
 *
 * @author Sir丶雨轩
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/flow/form")
public class FlowFormController {

    private final FlowFormService service;

    @GetMapping
    public Ps<FlowForm> page(FlowFormQuery query) {
        return Ps.ok(service.page(query));
    }

    @GetMapping("/{id}")
    public Rs<FlowForm> detail(@PathVariable String id) {
        return Rs.ok(service.getById(id));
    }

    @GetMapping("/key/{formKey}")
    public Rs<FlowForm> byKey(@PathVariable String formKey) {
        return Rs.ok(service.getByKey(formKey));
    }

    @PostMapping
    @OperationLog(title = "流程表单", type = BusinessType.CREATE)
    public Rs<Void> add(@RequestBody FlowForm data) {
        service.add(data);
        return Rs.ok();
    }

    @PutMapping
    @OperationLog(title = "流程表单", type = BusinessType.UPDATE)
    public Rs<Void> edit(@RequestBody FlowForm data) {
        service.edit(data);
        return Rs.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(title = "流程表单", type = BusinessType.DELETE)
    public Rs<Void> del(@PathVariable String id) {
        service.del(id);
        return Rs.ok();
    }
}
