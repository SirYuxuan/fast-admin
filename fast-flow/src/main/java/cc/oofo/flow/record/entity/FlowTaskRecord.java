package cc.oofo.flow.record.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程审批记录（审批意见 / 操作轨迹）。
 *
 * <p>Flowable 8 弃用了 Comment API，故用本表自行记录审批意见与动作，
 * 作为流程跟踪的可靠审计来源。</p>
 *
 * @author Sir丶雨轩
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("flow_task_record")
public class FlowTaskRecord extends BaseEntity<FlowTaskRecord> {

    /** 流程实例 ID */
    private String processInstanceId;
    /** 任务 ID */
    private String taskId;
    /** 任务节点名称 */
    private String taskName;
    /** 处理人 ID */
    private String assigneeId;
    /** 处理人名称 */
    private String assigneeName;
    /** 动作结果：start/approve/reject/transfer/cancel */
    private String outcome;
    /** 审批意见 */
    private String comment;
}
