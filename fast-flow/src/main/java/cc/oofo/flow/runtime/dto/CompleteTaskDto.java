package cc.oofo.flow.runtime.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 审批提交参数。
 *
 * @author Sir丶雨轩
 */
@Data
public class CompleteTaskDto {

    /** approve / reject */
    private String outcome;
    /** 审批意见 */
    private String comment;
    /** 表单变量 */
    private Map<String, Object> variables;
    /** 抄送用户 ID 列表 */
    private List<String> ccUserIds;
}
