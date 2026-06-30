package cc.oofo.flow.form.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义表单：以 JSON 描述字段，供发起 / 审批时动态渲染。
 *
 * <p>{@link #content} 为字段数组，元素结构与前端 VbenForm schema 对齐：
 * {@code {component,fieldName,label,required,options,...}}，通过 BPMN 节点的
 * {@code flowable:formKey} 与流程绑定。</p>
 *
 * @author Sir丶雨轩
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("flow_form")
public class FlowForm extends BaseEntity<FlowForm> {

    /** 表单标识（与 BPMN formKey 对应，全局唯一） */
    private String formKey;
    /** 表单名称 */
    private String name;
    /** 表单结构 JSON（字段数组） */
    private String content;
    /** 备注 */
    private String remark;
}
