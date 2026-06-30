package cc.oofo.flow.model.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cc.oofo.flow.model.entity.FlowModel;
import cc.oofo.flow.model.entity.query.FlowModelQuery;
import cc.oofo.flow.model.service.FlowModelService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.framework.web.response.Ps;
import cc.oofo.framework.web.response.Rs;
import cc.oofo.system.log.annotation.OperationLog;
import cc.oofo.system.log.enums.BusinessType;
import lombok.RequiredArgsConstructor;

/**
 * 流程模型管理接口。
 *
 * @author Sir丶雨轩
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/flow/model")
public class FlowModelController {

    private final FlowModelService service;

    @GetMapping
    public Ps<FlowModel> page(FlowModelQuery query) {
        return Ps.ok(service.page(query));
    }

    @GetMapping("/{id}")
    public Rs<FlowModel> detail(@PathVariable String id) {
        return Rs.ok(service.getById(id));
    }

    @PostMapping
    @OperationLog(title = "流程模型", type = BusinessType.CREATE)
    public Rs<Void> add(@RequestBody FlowModel data) {
        service.add(data);
        return Rs.ok();
    }

    @PutMapping
    @OperationLog(title = "流程模型", type = BusinessType.UPDATE)
    public Rs<Void> edit(@RequestBody FlowModel data) {
        service.edit(data);
        return Rs.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(title = "流程模型", type = BusinessType.DELETE)
    public Rs<Void> del(@PathVariable String id) {
        service.del(id);
        return Rs.ok();
    }

    /** 保存设计器的 BPMN XML。 */
    @PutMapping("/{id}/bpmn")
    @OperationLog(title = "流程模型-保存设计", type = BusinessType.UPDATE)
    public Rs<Void> saveBpmn(@PathVariable String id, @RequestBody Map<String, String> body) {
        String xml = body.get("bpmnXml");
        if (xml == null) {
            throw new BizException("bpmnXml 不能为空");
        }
        service.saveBpmn(id, xml);
        return Rs.ok();
    }

    /** 部署模型，生成新版本流程定义。 */
    @PostMapping("/{id}/deploy")
    @OperationLog(title = "流程模型-部署", type = BusinessType.OTHER)
    public Rs<String> deploy(@PathVariable String id) {
        Rs<String> rs = Rs.ok();
        rs.setData(service.deploy(id));
        return rs;
    }
}
