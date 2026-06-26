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
import java.util.Set;
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
    private static final Pattern SCHEMA_IDENTIFIER = Pattern.compile("[A-Za-z0-9_]+");
    private static final List<String> READONLY_SQL_PREFIXES = List.of(
            "select ", "show ", "desc ", "describe ", "explain ");
    private static final Set<String> SENSITIVE_COLUMN_KEYWORDS = Set.of(
            "api_key", "apikey", "password", "passwd", "secret", "token", "private_key");
    // 匹配 SQL 文本中直接引用的敏感列名（捕获别名场景：api_key AS k）
    private static final Pattern SENSITIVE_COLUMN_IN_SQL = Pattern.compile(
            "\\b(api_key|apikey|password|passwd|secret|token|private_key)\\b",
            Pattern.CASE_INSENSITIVE);

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

    public String executeAnySql(String sql, Map<String, Object> params, int maxRows) {
        if (!StringUtils.hasText(sql)) {
            throw new BizException("SQL 不能为空");
        }
        rejectIfSensitiveColumnsReferenced(sql);
        Map<String, Object> safeParams = params == null ? Map.of() : new LinkedHashMap<>(params);
        String normalized = sql.strip().toLowerCase();
        if (normalized.endsWith(";")) {
            normalized = normalized.substring(0, normalized.length() - 1).stripTrailing();
        }
        boolean isQuery = READONLY_SQL_PREFIXES.stream().anyMatch(normalized::startsWith);
        try {
            if (isQuery) {
                int limit = Math.min(Math.max(maxRows, 1), MAX_QUERY_ROWS);
                List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, safeParams);
                List<Map<String, Object>> limited = rows.size() > limit ? rows.subList(0, limit) : rows;
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("total", rows.size());
                result.put("returned", limited.size());
                result.put("rows", maskRows(limited));
                return objectMapper.writeValueAsString(result);
            } else {
                int affected = namedParameterJdbcTemplate.update(sql, safeParams);
                return "SQL 执行完成，影响行数：" + affected;
            }
        } catch (Exception e) {
            throw new BizException("SQL 执行失败：" + e.getMessage());
        }
    }

    public String executeReadOnlySql(String sql, Map<String, Object> params, int maxRows) {
        rejectIfSensitiveColumnsReferenced(sql);
        validateReadOnlySql(sql);
        int limit = Math.min(Math.max(maxRows, 1), MAX_QUERY_ROWS);
        Map<String, Object> safeParams = params == null ? Map.of() : new LinkedHashMap<>(params);

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, safeParams);
        List<Map<String, Object>> limited = rows.size() > limit ? rows.subList(0, limit) : rows;
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("total", rows.size());
            result.put("returned", limited.size());
            result.put("rows", maskRows(limited));
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new BizException("SQL 查询结果序列化失败");
        }
    }

    /**
     * 读取当前数据库的库表结构元数据。table 为空时返回所有表名与注释；
     * 指定 table 时返回该表的字段定义。仅查询 information_schema，不触碰业务数据。
     */
    public String describeSchema(String table) {
        try {
            if (!StringUtils.hasText(table)) {
                List<Map<String, Object>> tables = namedParameterJdbcTemplate.queryForList(
                        "SELECT table_name AS tableName, table_comment AS tableComment "
                                + "FROM information_schema.tables WHERE table_schema = DATABASE() "
                                + "ORDER BY table_name",
                        Map.of());
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("tableCount", tables.size());
                result.put("tables", tables);
                return objectMapper.writeValueAsString(result);
            }
            String trimmed = table.trim();
            if (!SCHEMA_IDENTIFIER.matcher(trimmed).matches()) {
                throw new BizException("表名仅允许字母、数字、下划线：" + table);
            }
            List<Map<String, Object>> columns = namedParameterJdbcTemplate.queryForList(
                    "SELECT column_name AS columnName, column_type AS columnType, is_nullable AS nullable, "
                            + "column_key AS columnKey, column_default AS columnDefault, "
                            + "column_comment AS columnComment "
                            + "FROM information_schema.columns "
                            + "WHERE table_schema = DATABASE() AND table_name = :table "
                            + "ORDER BY ordinal_position",
                    Map.of("table", trimmed));
            if (columns.isEmpty()) {
                throw new BizException("表不存在或没有字段：" + trimmed);
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("table", trimmed);
            result.put("columnCount", columns.size());
            result.put("columns", columns);
            return objectMapper.writeValueAsString(result);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("查询表结构失败：" + e.getMessage());
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
            result.put("rows", maskRows(limited));
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

    /**
     * 拒绝 SQL 中直接引用敏感列名的查询，防止通过别名绕过结果层脱敏。
     * 与 maskRows 配合形成双重保护：前者阻断显式引用，后者兜底 SELECT *。
     */
    private void rejectIfSensitiveColumnsReferenced(String sql) {
        Matcher m = SENSITIVE_COLUMN_IN_SQL.matcher(sql);
        if (m.find()) {
            throw new BizException("SQL 引用了敏感字段，禁止查询");
        }
    }

    private List<Map<String, Object>> maskRows(List<Map<String, Object>> rows) {
        return rows.stream().map(row -> {
            Map<String, Object> masked = new LinkedHashMap<>(row);
            masked.replaceAll((col, val) -> {
                if (val == null) return null;
                String lower = col.toLowerCase();
                return SENSITIVE_COLUMN_KEYWORDS.stream().anyMatch(lower::contains) ? "******" : val;
            });
            return masked;
        }).toList();
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
