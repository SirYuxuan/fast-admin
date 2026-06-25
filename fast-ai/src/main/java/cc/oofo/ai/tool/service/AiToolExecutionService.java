package cc.oofo.ai.tool.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.oofo.ai.tool.entity.AiToolConfig;
import cc.oofo.framework.exception.BizException;
import lombok.RequiredArgsConstructor;

/**
 * AI 工具执行服务。
 */
@Service
@RequiredArgsConstructor
public class AiToolExecutionService {

    public static final String TOOL_CONTEXT_PERMISSIONS = "permissionCodes";

    private static final int MAX_QUERY_ROWS = 100;
    private static final int MAX_HTTP_BODY_CHARS = 8000;
    private static final Pattern TEMPLATE_PARAM = Pattern.compile("\\{\\{\\s*([A-Za-z][A-Za-z0-9_]*)\\s*}}");
    private static final List<String> READONLY_SQL_PREFIXES = List.of(
            "select ", "show ", "desc ", "describe ", "explain ");

    private final AiToolConfigService toolConfigService;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ObjectMapper objectMapper;

    public String execute(String toolCode, Map<String, Object> args, Collection<String> permissionCodes) {
        AiToolConfig tool = toolConfigService.getEnabledByToolCode(toolCode);
        if (tool == null) {
            throw new BizException("AI 工具未启用或不存在：" + toolCode);
        }
        checkPermission(tool, permissionCodes);

        Map<String, Object> safeArgs = args == null ? Map.of() : new LinkedHashMap<>(args);
        if ("sql".equals(tool.getType())) {
            return executeSql(tool, safeArgs);
        }
        return executeHttp(tool, safeArgs);
    }

    public String executeReadOnlySql(String sql, Map<String, Object> params, int maxRows) {
        validateReadOnlySql(sql);
        int limit = Math.min(Math.max(maxRows, 1), MAX_QUERY_ROWS);
        Map<String, Object> safeParams = params == null ? Map.of() : new LinkedHashMap<>(params);

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, safeParams);
        List<Map<String, Object>> limited = rows.size() > limit ? rows.subList(0, limit) : rows;
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("total", rows.size());
            result.put("returned", limited.size());
            result.put("rows", limited);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new BizException("SQL 查询结果序列化失败");
        }
    }

    private void checkPermission(AiToolConfig tool, Collection<String> permissionCodes) {
        if (!StringUtils.hasText(tool.getPermissionCode())) {
            return;
        }
        if (permissionCodes == null || !permissionCodes.contains(tool.getPermissionCode())) {
            throw new BizException("无权调用 AI 工具：" + tool.getName());
        }
    }

    private String executeSql(AiToolConfig tool, Map<String, Object> args) {
        if (Boolean.FALSE.equals(tool.getReadOnly())) {
            int affected = namedParameterJdbcTemplate.update(tool.getSqlText(), args);
            return "SQL 执行完成，影响行数：" + affected;
        }

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(tool.getSqlText(), args);
        List<Map<String, Object>> limited = rows.size() > MAX_QUERY_ROWS ? rows.subList(0, MAX_QUERY_ROWS) : rows;
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("total", rows.size());
            result.put("rows", limited);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new BizException("SQL 查询结果序列化失败");
        }
    }

    private void validateReadOnlySql(String sql) {
        if (!StringUtils.hasText(sql)) {
            throw new BizException("SQL 不能为空");
        }
        String normalized = sql.strip();
        if (normalized.contains("--") || normalized.contains("#") || normalized.contains("/*")) {
            throw new BizException("只读 SQL 不允许包含注释");
        }
        if (hasMultipleStatements(normalized)) {
            throw new BizException("只读 SQL 只允许单条语句");
        }

        String lower = normalized.toLowerCase();
        if (lower.endsWith(";")) {
            lower = lower.substring(0, lower.length() - 1).stripTrailing();
        }
        boolean readOnly = READONLY_SQL_PREFIXES.stream().anyMatch(lower::startsWith);
        if (!readOnly) {
            throw new BizException("只读 SQL 仅允许 select/show/desc/describe/explain");
        }
    }

    private boolean hasMultipleStatements(String sql) {
        String normalized = sql.trim();
        if (normalized.endsWith(";")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.contains(";");
    }

    private String executeHttp(AiToolConfig tool, Map<String, Object> args) {
        try {
            int timeoutMs = tool.getTimeoutMs() == null ? 10000 : Math.min(Math.max(tool.getTimeoutMs(), 1000), 60000);
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMs))
                    .build();

            String url = renderTemplate(tool.getUrl(), args, true);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs));
            applyHeaders(builder, tool.getHeadersJson(), args);

            String method = StringUtils.hasText(tool.getMethod()) ? tool.getMethod().toUpperCase() : "GET";
            if ("GET".equals(method) || "DELETE".equals(method)) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                String body = renderTemplate(tool.getBodyTemplate(), args, false);
                builder.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
            }

            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return formatHttpResponse(response);
        } catch (Exception e) {
            throw new BizException("HTTP 工具调用失败：" + e.getMessage());
        }
    }

    private void applyHeaders(HttpRequest.Builder builder, String headersJson, Map<String, Object> args) throws Exception {
        if (!StringUtils.hasText(headersJson)) {
            return;
        }
        Map<String, Object> headers = objectMapper.readValue(headersJson, new TypeReference<>() {
        });
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            if (entry.getValue() != null) {
                builder.header(entry.getKey(), renderTemplate(String.valueOf(entry.getValue()), args, false));
            }
        }
    }

    private String formatHttpResponse(HttpResponse<String> response) throws Exception {
        String body = response.body();
        if (body != null && body.length() > MAX_HTTP_BODY_CHARS) {
            body = body.substring(0, MAX_HTTP_BODY_CHARS);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", response.statusCode());
        result.put("body", body);
        return objectMapper.writeValueAsString(result);
    }

    private String renderTemplate(String template, Map<String, Object> args, boolean urlEncode) {
        if (!StringUtils.hasText(template)) {
            return template;
        }
        Matcher matcher = TEMPLATE_PARAM.matcher(template);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            Object value = args.get(matcher.group(1));
            String text = value == null ? "" : String.valueOf(value);
            if (urlEncode) {
                text = URLEncoder.encode(text, StandardCharsets.UTF_8);
            }
            matcher.appendReplacement(builder, Matcher.quoteReplacement(text));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }
}
