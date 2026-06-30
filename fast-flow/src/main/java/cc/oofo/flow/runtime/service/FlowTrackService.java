package cc.oofo.flow.runtime.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import cc.oofo.flow.record.entity.FlowTaskRecord;
import cc.oofo.flow.record.mapper.FlowTaskRecordMapper;
import cc.oofo.framework.exception.BizException;
import lombok.RequiredArgsConstructor;

/**
 * 流程跟踪：返回 BPMN 图与高亮节点/连线，以及审批轨迹。
 *
 * @author Sir丶雨轩
 */
@Service
@RequiredArgsConstructor
public class FlowTrackService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final FlowTaskRecordMapper recordMapper;

    /**
     * 流程图高亮数据。
     *
     * @return {@code xml}=BPMN、{@code finishedIds}=已完成节点、
     *         {@code activeIds}=当前激活节点、{@code flowIds}=已走过的连线
     */
    public Map<String, Object> diagram(String instanceId) {
        HistoricProcessInstance p = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (p == null) {
            throw new BizException("流程实例不存在");
        }
        var def = repositoryService.getProcessDefinition(p.getProcessDefinitionId());
        String xml;
        try (InputStream in = repositoryService.getResourceAsStream(
                def.getDeploymentId(), def.getResourceName())) {
            xml = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BizException("读取流程图失败：" + e.getMessage());
        }

        Set<String> finished = new LinkedHashSet<>();
        Set<String> flows = new LinkedHashSet<>();
        for (HistoricActivityInstance a : historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceId).list()) {
            if ("sequenceFlow".equals(a.getActivityType())) {
                flows.add(a.getActivityId());
            } else if (a.getEndTime() != null) {
                finished.add(a.getActivityId());
            }
        }
        // 当前激活节点（运行中实例）
        Set<String> active = new LinkedHashSet<>();
        runtimeService.createExecutionQuery().processInstanceId(instanceId).list()
                .forEach(e -> active.addAll(runtimeService.getActiveActivityIds(e.getId())));
        finished.removeAll(active);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("xml", xml);
        data.put("finishedIds", new ArrayList<>(finished));
        data.put("activeIds", new ArrayList<>(active));
        data.put("flowIds", new ArrayList<>(flows));
        return data;
    }

    /** 审批轨迹（按时间正序）。 */
    public List<FlowTaskRecord> records(String instanceId) {
        return recordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FlowTaskRecord>()
                        .eq("process_instance_id", instanceId)
                        .orderByAsc("created_at"));
    }
}
