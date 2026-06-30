package cc.oofo.flow.runtime.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import cc.oofo.flow.common.FlowIdentity;
import cc.oofo.flow.record.entity.FlowTaskRecord;
import cc.oofo.flow.record.mapper.FlowTaskRecordMapper;
import cc.oofo.framework.exception.BizException;
import lombok.RequiredArgsConstructor;

/**
 * 流程发起与实例服务（我发起的、详情、撤销）。
 *
 * @author Sir丶雨轩
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FlowProcessService {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final FlowIdentity identity;
    private final FlowTaskRecordMapper recordMapper;

    /**
     * 按流程 key 发起一个实例。
     *
     * @param processKey 流程定义 key
     * @param variables  表单数据（作为流程变量）
     * @return 流程实例 ID
     */
    public String start(String processKey, Map<String, Object> variables) {
        if (!StringUtils.hasText(processKey)) {
            throw new BizException("流程标识不能为空");
        }
        ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey).latestVersion().active().singleResult();
        if (def == null) {
            throw new BizException("流程不存在或已被挂起：" + processKey);
        }
        Map<String, Object> vars = variables == null ? new HashMap<>() : new HashMap<>(variables);
        String userId = identity.userId();
        // 设置发起人，使 flowable:initiator 变量与历史 startUserId 生效
        Authentication.setAuthenticatedUserId(userId);
        try {
            ProcessInstance instance = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionId(def.getId())
                    .name(def.getName())
                    .variables(vars)
                    .start();
            record(instance.getId(), null, "发起", "start", null);
            return instance.getId();
        } finally {
            Authentication.setAuthenticatedUserId(null);
        }
    }

    /** 我发起的流程（分页）。 */
    public Map<String, Object> myInitiated(int page, int pageSize) {
        String userId = identity.userId();
        var query = historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId).orderByProcessInstanceStartTime().desc();
        long total = query.count();
        int first = Math.max(0, (page - 1) * pageSize);
        List<HistoricProcessInstance> list = query.listPage(first, pageSize);

        List<Map<String, Object>> items = new ArrayList<>(list.size());
        for (HistoricProcessInstance p : list) {
            items.add(toItem(p));
        }
        return pageData(total, items);
    }

    /** 流程实例详情：基本信息 + 变量。 */
    public Map<String, Object> detail(String instanceId) {
        HistoricProcessInstance p = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (p == null) {
            throw new BizException("流程实例不存在");
        }
        Map<String, Object> data = toItem(p);
        Map<String, Object> vars = new LinkedHashMap<>();
        for (HistoricVariableInstance v : historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(instanceId).list()) {
            vars.put(v.getVariableName(), v.getValue());
        }
        data.put("variables", vars);
        return data;
    }

    /** 撤销 / 终止流程实例（仅发起人或管理员）。 */
    public void cancel(String instanceId, String reason) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (instance == null) {
            throw new BizException("流程已结束或不存在");
        }
        runtimeService.deleteProcessInstance(instanceId,
                StringUtils.hasText(reason) ? reason : "发起人撤销");
        record(instanceId, null, "撤销", "cancel", reason);
    }

    private Map<String, Object> toItem(HistoricProcessInstance p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("processDefinitionId", p.getProcessDefinitionId());
        m.put("processDefinitionKey", p.getProcessDefinitionKey());
        m.put("name", p.getProcessDefinitionName());
        m.put("startUserId", p.getStartUserId());
        m.put("startUserName", identity.displayName(p.getStartUserId()));
        m.put("startTime", p.getStartTime());
        m.put("endTime", p.getEndTime());
        m.put("finished", p.getEndTime() != null);
        return m;
    }

    private void record(String instanceId, String taskId, String taskName, String outcome, String comment) {
        FlowTaskRecord r = new FlowTaskRecord();
        r.setProcessInstanceId(instanceId);
        r.setTaskId(taskId);
        r.setTaskName(taskName);
        r.setAssigneeId(identity.userId());
        r.setAssigneeName(identity.userName());
        r.setOutcome(outcome);
        r.setComment(comment);
        recordMapper.insert(r);
    }

    private Map<String, Object> pageData(long total, List<Map<String, Object>> items) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("items", items);
        return data;
    }
}
