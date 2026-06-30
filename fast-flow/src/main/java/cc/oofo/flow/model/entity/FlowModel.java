package cc.oofo.flow.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程模型实体。
 *
 * <p>承载流程设计器产出的 BPMN XML 与元信息；部署时由 {@code RepositoryService}
 * 写入 Flowable 的 {@code ACT_RE_*} 表，本表只保存设计态数据。</p>
 *
 * @author Sir丶雨轩
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("flow_model")
public class FlowModel extends BaseEntity<FlowModel> {

    /** 模型名称 */
    private String name;
    /** 流程标识（BPMN process id，部署后即流程定义 key） */
    private String modelKey;
    /** 分类 */
    private String category;
    /** BPMN 2.0 XML */
    private String bpmnXml;
    /** 描述 */
    private String description;
    /** 最近一次部署 ID（ACT_RE_DEPLOYMENT.ID_） */
    private String latestDeployId;
    /** 最近一次流程定义 ID（ACT_RE_PROCDEF.ID_） */
    private String latestDefinitionId;
    /** 模型状态：1启用 0停用 */
    private Integer status;
}
