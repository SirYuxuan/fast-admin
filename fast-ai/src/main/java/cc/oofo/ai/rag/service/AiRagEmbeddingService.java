package cc.oofo.ai.rag.service;

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
 * OpenAI-compatible embedding 客户端。
 */
@Service
@RequiredArgsConstructor
public class AiRagEmbeddingService {

    private final AiRagConfigService configService;
    private final ObjectMapper objectMapper;

    public List<Double> embed(String input) {
        if (!StringUtils.hasText(input)) {
            throw new BizException("Embedding 文本不能为空");
        }
        ResolvedEmbeddingConfig config = resolveConfig();
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", config.model(),
                    "input", input));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.baseUrl() + "/embeddings"))
                    .timeout(Duration.ofMillis(config.timeoutMs()))
                    .header("Authorization", "Bearer " + config.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(config.timeoutMs()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new BizException("Embedding 请求失败（HTTP " + response.statusCode()
                        + "）：请检查 Base URL 是否支持 /v1/embeddings，模型是否为 embedding 模型。"
                        + snippet(response.body()));
            }
            JsonNode embedding = objectMapper.readTree(response.body()).path("data").path(0).path("embedding");
            if (!embedding.isArray()) {
                throw new BizException("Embedding 返回格式不支持");
            }
            List<Double> vector = new ArrayList<>();
            for (JsonNode value : embedding) {
                vector.add(value.asDouble());
            }
            if (vector.isEmpty()) {
                throw new BizException("Embedding 向量为空");
            }
            return vector;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("Embedding 请求失败：" + e.getMessage());
        }
    }

    private ResolvedEmbeddingConfig resolveConfig() {
        String baseUrl = configService.getEmbeddingBaseUrl();
        String apiKey = configService.getEmbeddingApiKey();
        String model = configService.getEmbeddingModel();

        if (!StringUtils.hasText(baseUrl)) {
            throw new BizException("Embedding Base URL 不能为空，请在 AI 运维的 AI 配置页面配置。"
                    + "RAG 需要独立的 embedding 模型，不能直接复用当前聊天模型");
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new BizException("Embedding API Key 不能为空，请在 AI 运维的 AI 配置页面配置");
        }
        if (!StringUtils.hasText(model)) {
            throw new BizException("Embedding 模型不能为空");
        }
        return new ResolvedEmbeddingConfig(openAiBase(baseUrl), apiKey, model, configService.getEmbeddingTimeoutMs());
    }

    private String openAiBase(String baseUrl) {
        String base = baseUrl.trim().replaceAll("/+$", "");
        return base.endsWith("/v1") ? base : base + "/v1";
    }

    private String snippet(String body) {
        if (!StringUtils.hasText(body)) {
            return "无响应内容";
        }
        String content = body.length() > 500 ? body.substring(0, 500) : body;
        return "响应内容：" + content;
    }

    private record ResolvedEmbeddingConfig(String baseUrl, String apiKey, String model, int timeoutMs) {
    }
}
