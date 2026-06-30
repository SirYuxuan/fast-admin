package cc.oofo.flow.cc.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程抄送记录：把某次审批抄送给指定用户，用于「抄送我的」事项。
 *
 * @author Sir丶雨轩
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("flow_cc_record")
public class FlowCcRecord extends BaseEntity<FlowCcRecord> {

    /** 流程实例 ID */
    private String processInstanceId;
    /** 流程名称 */
    private String processName;
    /** 触发抄送的任务节点名称 */
    private String taskName;
    /** 被抄送人 ID */
    private String ccUserId;
    /** 被抄送人名称 */
    private String ccUserName;
    /** 抄送发起人名称 */
    private String creatorName;
    /** 是否已读：1已读 0未读 */
    private Integer isRead;
}
