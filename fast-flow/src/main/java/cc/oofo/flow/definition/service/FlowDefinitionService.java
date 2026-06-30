package cc.oofo.flow.definition.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import cc.oofo.flow.form.entity.FlowForm;
import cc.oofo.flow.form.service.FlowFormService;
import cc.oofo.framework.exception.BizException;
import lombok.RequiredArgsConstructor;

/**
 * 流程定义服务：查询/挂起/激活/删除已部署到 Flowable 的流程定义。
 *
 * @author Sir丶雨轩
 */
@Service
@RequiredArgsConstructor
public class FlowDefinitionService {

    private final RepositoryService repositoryService;
    private final FlowFormService formService;

    /** 可发起的流程列表（每个 key 最新且激活的版本）。 */
    public List<Map<String, Object>> startable(String name) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .latestVersion().active();
        if (name != null && !name.isBlank()) {
            query.processDefinitionNameLike("%" + name + "%");
        }
        List<ProcessDefinition> list = query.orderByProcessDefinitionKey().asc().list();
        List<Map<String, Object>> items = new ArrayList<>(list.size());
        for (ProcessDefinition d : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("key", d.getKey());
            m.put("name", d.getName());
            m.put("version", d.getVersion());
            m.put("category", d.getCategory());
            items.add(m);
        }
        return items;
    }

    /** 取流程发起表单（开始节点 flowable:formKey 绑定的自定义表单）。 */
    public Map<String, Object> startForm(String definitionId) {
        BpmnModel model = repositoryService.getBpmnModel(definitionId);
        String formKey = null;
        Process process = model.getMainProcess();
        if (process != null) {
            for (FlowElement e : process.getFlowElements()) {
                if (e instanceof StartEvent se) {
                    formKey = se.getFormKey();
                    break;
                }
            }
        }
        FlowForm form = formService.getByKey(formKey);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("formKey", formKey);
        data.put("form", form == null ? null : form.getContent());
        return data;
    }

    /**
     * 分页查询流程定义（默认每个流程只返回最新版本，避免版本平铺造成「重复」错觉）。
     */
    public Map<String, Object> page(int page, int pageSize, String name, String key) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .latestVersion();
        if (name != null && !name.isBlank()) {
            query.processDefinitionNameLike("%" + name + "%");
        }
        if (key != null && !key.isBlank()) {
            query.processDefinitionKeyLike("%" + key + "%");
        }
        long total = query.count();
        int first = Math.max(0, (page - 1) * pageSize);
        List<ProcessDefinition> list = query
                .orderByProcessDefinitionKey().asc()
                .listPage(first, pageSize);

        List<Map<String, Object>> items = new ArrayList<>(list.size());
        for (ProcessDefinition d : list) {
            items.add(toItem(d));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("items", items);
        return data;
    }

    /** 某流程的全部历史版本（按版本倒序），供「历史版本」抽屉使用。 */
    public List<Map<String, Object>> versions(String key) {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .orderByProcessDefinitionVersion().desc()
                .list();
        List<Map<String, Object>> items = new ArrayList<>(list.size());
        for (ProcessDefinition d : list) {
            items.add(toItem(d));
        }
        return items;
    }

    private Map<String, Object> toItem(ProcessDefinition d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("key", d.getKey());
        m.put("name", d.getName());
        m.put("version", d.getVersion());
        m.put("category", d.getCategory());
        m.put("deploymentId", d.getDeploymentId());
        m.put("suspended", d.isSuspended());
        return m;
    }

    /** 挂起：挂起后无法发起新实例。 */
    public void suspend(String definitionId) {
        ProcessDefinition def = get(definitionId);
        if (def.isSuspended()) {
            throw new BizException("该版本已是挂起状态");
        }
        repositoryService.suspendProcessDefinitionById(definitionId, true, null);
    }

    /** 激活。 */
    public void activate(String definitionId) {
        ProcessDefinition def = get(definitionId);
        if (!def.isSuspended()) {
            throw new BizException("该版本已是激活状态");
        }
        repositoryService.activateProcessDefinitionById(definitionId, true, null);
    }

    /** 删除部署（cascade=true 会级联删除运行中实例与历史）。 */
    public void delete(String deploymentId, boolean cascade) {
        repositoryService.deleteDeployment(deploymentId, cascade);
    }

    /** 读取流程定义的 BPMN XML（用于查看/再次设计）。 */
    public String getBpmnXml(String definitionId) {
        ProcessDefinition def = get(definitionId);
        try (InputStream in = repositoryService.getResourceAsStream(
                def.getDeploymentId(), def.getResourceName())) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BizException("读取流程 XML 失败：" + e.getMessage());
        }
    }

    private ProcessDefinition get(String definitionId) {
        ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(definitionId).singleResult();
        if (def == null) {
            throw new BizException("流程定义不存在");
        }
        return def;
    }
}
