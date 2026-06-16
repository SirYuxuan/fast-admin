package cc.oofo.ai.tool.service;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.oofo.ai.tool.dto.AiToolConfigSaveDto;
import cc.oofo.ai.tool.entity.AiToolConfig;
import cc.oofo.ai.tool.entity.query.AiToolConfigQuery;
import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import lombok.RequiredArgsConstructor;

/**
 * AI 工具配置服务。
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AiToolConfigService extends BaseService<AiToolConfig> {

    private static final Pattern TOOL_CODE_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{1,63}$");
    private static final Set<String> TYPES = Set.of("sql", "http");
    private static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE");

    private final ObjectMapper objectMapper;

    public Page<AiToolConfig> page(AiToolConfigQuery query) {
        query.getQueryWrapper().orderByDesc("enabled").orderByDesc("created_at");
        return page(query.getMPPage(), query.getQueryWrapper());
    }

    public void add(AiToolConfigSaveDto dto) {
        validate(dto, null);
        if (toolCodeExists(null, dto.getToolCode())) {
            throw new BizException("工具编码已存在");
        }

        AiToolConfig entity = new AiToolConfig();
        copyToEntity(dto, entity);
        entity.setEnabled(dto.getEnabled() == null || dto.getEnabled());
        save(entity);
    }

    public void update(AiToolConfigSaveDto dto) {
        AiToolConfig entity = getByIdOrThrow(dto.getId());
        validate(dto, entity.getId());
        if (toolCodeExists(entity.getId(), dto.getToolCode())) {
            throw new BizException("工具编码已存在");
        }

        copyToEntity(dto, entity);
        updateById(entity);
    }

    public void del(String id) {
        getByIdOrThrow(id);
        removeById(id);
    }

    public AiToolConfig getByIdOrThrow(String id) {
        AiToolConfig entity = getById(id);
        if (entity == null) {
            throw new BizException("AI 工具不存在");
        }
        return entity;
    }

    public AiToolConfig getEnabledByToolCode(String toolCode) {
        return getOne(new LambdaQueryWrapper<AiToolConfig>()
                .eq(AiToolConfig::getToolCode, toolCode)
                .eq(AiToolConfig::getEnabled, true)
                .last("limit 1"));
    }

    public List<AiToolConfig> listEnabled() {
        return list(new LambdaQueryWrapper<AiToolConfig>()
                .eq(AiToolConfig::getEnabled, true)
                .orderByAsc(AiToolConfig::getToolCode));
    }

    private void validate(AiToolConfigSaveDto dto, String id) {
        if (dto == null) {
            throw new BizException("AI 工具配置不能为空");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new BizException("工具名称不能为空");
        }
        if (!StringUtils.hasText(dto.getToolCode()) || !TOOL_CODE_PATTERN.matcher(dto.getToolCode()).matches()) {
            throw new BizException("工具编码只能使用英文字母、数字和下划线，并且以字母开头");
        }
        if (!StringUtils.hasText(dto.getType()) || !TYPES.contains(dto.getType())) {
            throw new BizException("工具类型不支持");
        }
        if (!StringUtils.hasText(dto.getDescription())) {
            throw new BizException("工具说明不能为空");
        }

        if ("sql".equals(dto.getType())) {
            validateSql(dto);
        } else {
            validateHttp(dto);
        }
    }

    private void validateSql(AiToolConfigSaveDto dto) {
        if (!StringUtils.hasText(dto.getSqlText())) {
            throw new BizException("SQL 工具必须填写 SQL 模板");
        }
        if (hasMultipleStatements(dto.getSqlText())) {
            throw new BizException("SQL 工具只允许配置单条语句");
        }
        if (!Boolean.FALSE.equals(dto.getReadOnly()) && !isReadOnlySql(dto.getSqlText())) {
            throw new BizException("只读 SQL 工具仅允许 select/show/desc/describe/explain");
        }
    }

    private void validateHttp(AiToolConfigSaveDto dto) {
        String method = StringUtils.hasText(dto.getMethod()) ? dto.getMethod().toUpperCase() : null;
        if (!StringUtils.hasText(method) || !HTTP_METHODS.contains(method)) {
            throw new BizException("HTTP 方法不支持");
        }
        if (!StringUtils.hasText(dto.getUrl())
                || !(dto.getUrl().startsWith("http://") || dto.getUrl().startsWith("https://"))) {
            throw new BizException("HTTP 地址必须以 http:// 或 https:// 开头");
        }
        validateJsonObject(dto.getHeadersJson(), "请求头 JSON 必须是对象");
    }

    private void validateJsonObject(String json, String message) {
        if (!StringUtils.hasText(json)) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isObject()) {
                throw new BizException(message);
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(message);
        }
    }

    private boolean toolCodeExists(String excludeId, String toolCode) {
        LambdaQueryWrapper<AiToolConfig> wrapper = new LambdaQueryWrapper<AiToolConfig>()
                .eq(AiToolConfig::getToolCode, toolCode);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(AiToolConfig::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    private boolean hasMultipleStatements(String sql) {
        String normalized = sql.trim();
        if (normalized.endsWith(";")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.contains(";");
    }

    private boolean isReadOnlySql(String sql) {
        String normalized = sql.stripLeading().toLowerCase();
        return normalized.startsWith("select ")
                || normalized.startsWith("show ")
                || normalized.startsWith("desc ")
                || normalized.startsWith("describe ")
                || normalized.startsWith("explain ");
    }

    private void copyToEntity(AiToolConfigSaveDto dto, AiToolConfig entity) {
        entity.setName(dto.getName());
        entity.setToolCode(dto.getToolCode());
        entity.setType(dto.getType());
        entity.setDescription(dto.getDescription());
        if (dto.getEnabled() != null) {
            entity.setEnabled(dto.getEnabled());
        }
        entity.setPermissionCode(dto.getPermissionCode());
        entity.setMethod(StringUtils.hasText(dto.getMethod()) ? dto.getMethod().toUpperCase() : null);
        entity.setUrl(dto.getUrl());
        entity.setHeadersJson(dto.getHeadersJson());
        entity.setBodyTemplate(dto.getBodyTemplate());
        entity.setSqlText(dto.getSqlText());
        entity.setReadOnly(dto.getReadOnly() == null || dto.getReadOnly());
        entity.setTimeoutMs(dto.getTimeoutMs() == null ? 10000 : dto.getTimeoutMs());
        entity.setRemark(dto.getRemark());
    }
}
