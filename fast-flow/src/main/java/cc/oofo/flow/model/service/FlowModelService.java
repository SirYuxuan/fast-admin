package cc.oofo.flow.model.service;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cc.oofo.flow.model.entity.FlowModel;
import cc.oofo.flow.model.entity.query.FlowModelQuery;
import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import lombok.RequiredArgsConstructor;

/**
 * 流程模型服务：设计态 CRUD 与部署到 Flowable 引擎。
 *
 * @author Sir丶雨轩
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FlowModelService extends BaseService<FlowModel> {

    private final RepositoryService repositoryService;

    /** 新模型的初始 BPMN 模板：占位 {@code %s}=流程 key、name。 */
    private static final String DEFAULT_BPMN = """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                         xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
                         xmlns:flowable="http://flowable.org/bpmn"
                         targetNamespace="http://flowable.org/processdef">
              <process id="%s" name="%s" isExecutable="true">
                <startEvent id="startEvent1" flowable:initiator="initiator"/>
              </process>
              <bpmndi:BPMNDiagram id="BPMNDiagram_%s">
                <bpmndi:BPMNPlane bpmnElement="%s" id="BPMNPlane_%s">
                  <bpmndi:BPMNShape bpmnElement="startEvent1" id="BPMNShape_startEvent1">
                    <omgdc:Bounds height="30" width="30" x="173" y="158"/>
                  </bpmndi:BPMNShape>
                </bpmndi:BPMNPlane>
              </bpmndi:BPMNDiagram>
            </definitions>
            """;

    public Page<FlowModel> page(FlowModelQuery query) {
        query.getQueryWrapper().orderByDesc("created_at");
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    public void add(FlowModel data) {
        if (!StringUtils.hasText(data.getName())) {
            throw new BizException("模型名称不能为空");
        }
        if (!StringUtils.hasText(data.getModelKey())) {
            throw new BizException("流程标识不能为空");
        }
        if (keyExists(null, data.getModelKey())) {
            throw new BizException("流程标识已存在");
        }
        if (!StringUtils.hasText(data.getBpmnXml())) {
            String key = data.getModelKey();
            data.setBpmnXml(DEFAULT_BPMN.formatted(key, data.getName(), key, key, key));
        }
        if (data.getStatus() == null) {
            data.setStatus(1);
        }
        save(data);
    }

    public void edit(FlowModel data) {
        if (!StringUtils.hasText(data.getId())) {
            throw new BizException("ID 不能为空");
        }
        if (StringUtils.hasText(data.getModelKey()) && keyExists(data.getId(), data.getModelKey())) {
            throw new BizException("流程标识已存在");
        }
        updateById(data);
    }

    public void del(String id) {
        removeById(id);
    }

    /** 仅保存设计器产出的 BPMN XML，不触发部署。 */
    public void saveBpmn(String id, String bpmnXml) {
        FlowModel model = getById(id);
        if (model == null) {
            throw new BizException("模型不存在");
        }
        FlowModel update = new FlowModel();
        update.setId(id);
        update.setBpmnXml(bpmnXml);
        updateById(update);
    }

    /**
     * 将模型当前 BPMN 部署到 Flowable，生成新版本流程定义。
     *
     * @return 新生成的流程定义 ID
     */
    public String deploy(String id) {
        FlowModel model = getById(id);
        if (model == null) {
            throw new BizException("模型不存在");
        }
        if (!StringUtils.hasText(model.getBpmnXml())) {
            throw new BizException("流程内容为空，请先设计流程");
        }
        Deployment deployment = repositoryService.createDeployment()
                .name(model.getName())
                .key(model.getModelKey())
                .category(model.getCategory())
                .addString(model.getModelKey() + ".bpmn20.xml", model.getBpmnXml())
                .deploy();

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        if (definition == null) {
            throw new BizException("部署失败：未生成流程定义，请检查流程是否包含可执行的 process 节点");
        }

        FlowModel update = new FlowModel();
        update.setId(id);
        update.setLatestDeployId(deployment.getId());
        update.setLatestDefinitionId(definition.getId());
        updateById(update);
        return definition.getId();
    }

    private boolean keyExists(String excludeId, String key) {
        return query().ne(StringUtils.hasText(excludeId), "id", excludeId)
                .eq("model_key", key).exists();
    }
}
