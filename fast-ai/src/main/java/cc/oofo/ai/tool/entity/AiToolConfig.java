package cc.oofo.ai.tool.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import cc.oofo.framework.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 工具配置。
 *
 * <p>前台只负责维护白名单工具，真正的 SQL/API 调用统一在后端按配置执行。</p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_tool_config")
public class AiToolConfig extends BaseEntity<AiToolConfig> {

    /** 展示名称，方便运维识别。 */
    private String name;

    /** 工具编码，会暴露给模型作为 tool name，建议使用英文和下划线。 */
    private String toolCode;

    /** 工具类型：sql / http。 */
    private String type;

    /** 工具说明，会作为模型选择工具时的描述。 */
    private String description;

    /** 是否启用。 */
    private Boolean enabled;

    /** 调用该工具需要的权限编码，空表示登录用户均可调用。 */
    private String permissionCode;

    /** HTTP 方法：GET/POST/PUT/PATCH/DELETE。 */
    private String method;

    /** HTTP 地址模板，可使用 {{param}} 占位。 */
    private String url;

    /** HTTP 请求头 JSON 对象，可使用 {{param}} 占位。 */
    private String headersJson;

    /** HTTP 请求体模板，可使用 {{param}} 占位。 */
    private String bodyTemplate;

    /** SQL 模板，使用 :param 命名参数。 */
    private String sqlText;

    /** SQL 是否只读，只读时仅允许查询类语句。 */
    private Boolean readOnly;

    /** 调用超时时间，单位毫秒。 */
    private Integer timeoutMs;

    /** 备注。 */
    private String remark;

    /** 是否系统内置工具。 */
    private Boolean systemBuiltin;
}
