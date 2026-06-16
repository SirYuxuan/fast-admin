package cc.oofo.ai.model.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.oofo.framework.exception.BizException;
import lombok.RequiredArgsConstructor;

/**
 * 通过 HTTP 直连模型服务，提供「获取模型列表」与「连通性测试」能力。
 *
 * <p>不依赖 Spring AI 的 ChatModel，避免构建客户端的副作用，便于在保存前对填写的链接和密钥做校验。</p>
 */
@Service
@RequiredArgsConstructor
public class AiModelProbeService {

    private static final String ANTHROPIC_DEFAULT_BASE = "https://api.anthropic.com";
    private static final String OPENAI_DEFAULT_BASE = "https://api.openai.com";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final int MAX_ERROR_CHARS = 500;

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    /**
     * 拉取服务方可用的模型列表。
     */
    public List<String> fetchModels(String provider, String baseUrl, String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BizException("API Key 不能为空");
        }
        String url = modelsUrl(provider, baseUrl);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .GET();
        applyAuthHeaders(builder, provider, apiKey);

        JsonNode root = send(builder.build(), "获取模型列表");
        JsonNode data = root.path("data");
        if (!data.isArray()) {
            throw new BizException("模型列表返回格式不支持");
        }
        List<String> models = new ArrayList<>();
        for (JsonNode item : data) {
            String id = item.path("id").asText(null);
            if (StringUtils.hasText(id)) {
                models.add(id);
            }
        }
        return models;
    }

    /**
     * 发送一次最小请求测试连通性并返回延时（毫秒）。
     */
    public long test(String provider, String baseUrl, String apiKey, String model) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BizException("API Key 不能为空");
        }
        if (!StringUtils.hasText(model)) {
            throw new BizException("模型名称不能为空");
        }
        String url;
        String body;
        if (isAnthropic(provider)) {
            url = trimBase(baseUrl, ANTHROPIC_DEFAULT_BASE) + "/v1/messages";
            body = anthropicProbeBody(model);
        } else {
            url = openAiBase(baseUrl, provider) + "/chat/completions";
            body = openAiProbeBody(model);
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        applyAuthHeaders(builder, provider, apiKey);

        long start = System.currentTimeMillis();
        send(builder.build(), "模型测试");
        return System.currentTimeMillis() - start;
    }

    private String modelsUrl(String provider, String baseUrl) {
        if (isAnthropic(provider)) {
            return trimBase(baseUrl, ANTHROPIC_DEFAULT_BASE) + "/v1/models";
        }
        return openAiBase(baseUrl, provider) + "/models";
    }

    private void applyAuthHeaders(HttpRequest.Builder builder, String provider, String apiKey) {
        if (isAnthropic(provider)) {
            builder.header("x-api-key", apiKey);
            builder.header("anthropic-version", ANTHROPIC_VERSION);
        } else {
            builder.header("Authorization", "Bearer " + apiKey);
        }
    }

    private JsonNode send(HttpRequest request, String action) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            if (response.statusCode() >= 400) {
                throw new BizException(action + "失败（HTTP " + response.statusCode() + "）：" + snippet(responseBody));
            }
            return objectMapper.readTree(responseBody == null ? "{}" : responseBody);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(action + "失败：" + e.getMessage());
        }
    }

    private boolean isAnthropic(String provider) {
        return "anthropic".equals(provider);
    }

    /**
     * OpenAI 兼容接口需保证以 /v1 结尾，缺省补齐。
     */
    private String openAiBase(String baseUrl, String provider) {
        String base = baseUrl;
        if (!StringUtils.hasText(base)) {
            if ("openai-compatible".equals(provider)) {
                throw new BizException("OpenAI 兼容模型需填写 Base URL");
            }
            base = OPENAI_DEFAULT_BASE;
        }
        base = stripTrailingSlash(base.trim());
        if (!base.endsWith("/v1")) {
            base = base + "/v1";
        }
        return base;
    }

    private String trimBase(String baseUrl, String defaultBase) {
        String base = StringUtils.hasText(baseUrl) ? baseUrl.trim() : defaultBase;
        return stripTrailingSlash(base);
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String openAiProbeBody(String model) {
        return toJson(Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", "ping")),
                "max_tokens", 1));
    }

    private String anthropicProbeBody(String model) {
        return toJson(Map.of(
                "model", model,
                "max_tokens", 1,
                "messages", List.of(Map.of("role", "user", "content", "ping"))));
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new BizException("构建测试请求失败");
        }
    }

    private String snippet(String body) {
        if (!StringUtils.hasText(body)) {
            return "无响应内容";
        }
        return body.length() > MAX_ERROR_CHARS ? body.substring(0, MAX_ERROR_CHARS) : body;
    }
}
