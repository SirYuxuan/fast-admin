package cc.oofo.flow.runtime.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import cc.oofo.flow.cc.entity.FlowCcRecord;
import cc.oofo.flow.cc.mapper.FlowCcRecordMapper;
import cc.oofo.flow.common.FlowIdentity;
import cc.oofo.flow.form.entity.FlowForm;
import cc.oofo.flow.form.service.FlowFormService;
import cc.oofo.flow.record.entity.FlowTaskRecord;
import cc.oofo.flow.record.mapper.FlowTaskRecordMapper;
import cc.oofo.framework.exception.BizException;
import lombok.RequiredArgsConstructor;

/**
 * 待办任务服务：待办 / 已办、审批（同意/驳回）、转办、签收、抄送。
 *
 * @author Sir丶雨轩
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FlowTaskService {

    private final TaskService taskService;
    private final HistoryService historyService;
    private final RuntimeService runtimeService;
    private final FlowIdentity identity;
    private final FlowFormService formService;
    private final FlowTaskRecordMapper recordMapper;
    private final FlowCcRecordMapper ccMapper;

    /** 我的待办（指派给我 / 我可签收 / 我所在角色组）。 */
    public Map<String, Object> todo(int page, int pageSize) {
        String userId = identity.userId();
        List<String> groups = identity.groupIds();

        TaskQuery query = taskService.createTaskQuery().active();
        query = query.or().taskAssignee(userId).taskCandidateUser(userId);
        if (!CollectionUtils.isEmpty(groups)) {
            query = query.taskCandidateGroupIn(groups);
        }
        query = query.endOr().orderByTaskCreateTime().desc();

        long total = query.count();
        int first = Math.max(0, (page - 1) * pageSize);
        List<Task> list = query.listPage(first, pageSize);

        Map<String, String> nameCache = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>(list.size());
        for (Task t : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("taskId", t.getId());
            m.put("taskName", t.getName());
            m.put("processInstanceId", t.getProcessInstanceId());
            m.put("processName", processName(t.getProcessInstanceId(), nameCache));
            m.put("assignee", t.getAssignee());
            m.put("claimed", StringUtils.hasText(t.getAssignee()));
            m.put("createTime", t.getCreateTime());
            items.add(m);
        }
        return pageData(total, items);
    }

    /** 我的已办。 */
    public Map<String, Object> done(int page, int pageSize) {
        String userId = identity.userId();
        var query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId).finished()
                .orderByHistoricTaskInstanceEndTime().desc();
        long total = query.count();
        int first = Math.max(0, (page - 1) * pageSize);
        List<HistoricTaskInstance> list = query.listPage(first, pageSize);

        Map<String, String> nameCache = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>(list.size());
        for (HistoricTaskInstance t : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("taskId", t.getId());
            m.put("taskName", t.getName());
            m.put("processInstanceId", t.getProcessInstanceId());
            m.put("processName", processName(t.getProcessInstanceId(), nameCache));
            m.put("startTime", t.getCreateTime());
            m.put("endTime", t.getEndTime());
            items.add(m);
        }
        return pageData(total, items);
    }

    /** 任务详情：节点信息 + 绑定表单 + 当前变量。 */
    public Map<String, Object> taskDetail(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BizException("任务不存在或已被处理");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskId", task.getId());
        data.put("taskName", task.getName());
        data.put("processInstanceId", task.getProcessInstanceId());
        data.put("assignee", task.getAssignee());
        data.put("variables", runtimeService.getVariables(task.getExecutionId()));

        FlowForm form = formService.getByKey(task.getFormKey());
        data.put("formKey", task.getFormKey());
        data.put("form", form == null ? null : form.getContent());
        return data;
    }

    /**
     * 审批任务。
     *
     * @param taskId    任务 ID
     * @param outcome   approve / reject（写入流程变量 {@code outcome} 供网关判断）
     * @param comment   审批意见
     * @param variables 表单变量
     * @param ccUserIds 抄送用户 ID 列表
     */
    public void complete(String taskId, String outcome, String comment,
            Map<String, Object> variables, List<String> ccUserIds) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BizException("任务不存在或已被处理");
        }
        String userId = identity.userId();
        // 未签收的任务先签收，确保已办可追溯到处理人
        if (!StringUtils.hasText(task.getAssignee())) {
            taskService.claim(taskId, userId);
        } else if (!userId.equals(task.getAssignee())) {
            throw new BizException("该任务已被他人签收，无法处理");
        }

        Map<String, Object> vars = variables == null ? new HashMap<>() : new HashMap<>(variables);
        vars.put("outcome", StringUtils.hasText(outcome) ? outcome : "approve");
        taskService.complete(taskId, vars);

        record(task, "reject".equals(outcome) ? "reject" : "approve", comment);
        addCc(task, ccUserIds);
    }

    /** 转办给他人。 */
    public void transfer(String taskId, String toUserId, String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BizException("任务不存在或已被处理");
        }
        if (!StringUtils.hasText(toUserId)) {
            throw new BizException("转办目标用户不能为空");
        }
        taskService.setAssignee(taskId, toUserId);
        record(task, "transfer", "转办给 " + identity.displayName(toUserId)
                + (StringUtils.hasText(comment) ? "：" + comment : ""));
    }

    /** 签收任务。 */
    public void claim(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BizException("任务不存在或已被处理");
        }
        if (StringUtils.hasText(task.getAssignee())) {
            throw new BizException("任务已被签收");
        }
        taskService.claim(taskId, identity.userId());
    }

    private void addCc(Task task, List<String> ccUserIds) {
        if (CollectionUtils.isEmpty(ccUserIds)) {
            return;
        }
        String processName = processName(task.getProcessInstanceId(), new HashMap<>());
        for (String uid : ccUserIds) {
            if (!StringUtils.hasText(uid)) {
                continue;
            }
            FlowCcRecord cc = new FlowCcRecord();
            cc.setProcessInstanceId(task.getProcessInstanceId());
            cc.setProcessName(processName);
            cc.setTaskName(task.getName());
            cc.setCcUserId(uid);
            cc.setCcUserName(identity.displayName(uid));
            cc.setCreatorName(identity.userName());
            cc.setIsRead(0);
            ccMapper.insert(cc);
        }
    }

    private void record(Task task, String outcome, String comment) {
        FlowTaskRecord r = new FlowTaskRecord();
        r.setProcessInstanceId(task.getProcessInstanceId());
        r.setTaskId(task.getId());
        r.setTaskName(task.getName());
        r.setAssigneeId(identity.userId());
        r.setAssigneeName(identity.userName());
        r.setOutcome(outcome);
        r.setComment(comment);
        recordMapper.insert(r);
    }

    private String processName(String instanceId, Map<String, String> cache) {
        return cache.computeIfAbsent(instanceId, id -> {
            HistoricProcessInstance p = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(id).singleResult();
            return p == null ? "" : p.getProcessDefinitionName();
        });
    }

    private Map<String, Object> pageData(long total, List<Map<String, Object>> items) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("items", items);
        return data;
    }
}
