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

import cc.oofo.ai.rag.dto.AiRagVectorStoreStatusDto;
import cc.oofo.framework.exception.BizException;
import lombok.RequiredArgsConstructor;

/**
 * Qdrant REST 探测服务。
 */
@Service
@RequiredArgsConstructor
public class AiRagQdrantService {

    private final AiRagConfigService configService;
    private final ObjectMapper objectMapper;

    public AiRagVectorStoreStatusDto status() {
        AiRagVectorStoreStatusDto status = new AiRagVectorStoreStatusDto();
        status.setEnabled(configService.isEnabled());
        status.setUrl(baseUrl());
        status.setDefaultCollection(configService.getCollectionName());
        if (!configService.isEnabled()) {
            status.setMessage("AI 知识库未启用");
            return status;
        }

        long start = System.currentTimeMillis();
        try {
            JsonNode root = sendGet("/");
            List<String> collections = collections();
            status.setConnected(true);
            status.setVersion(root.path("version").asText(null));
            status.setStatus(root.path("status").asText("ok"));
            status.setCollections(collections);
            status.setDefaultCollectionExists(collections.contains(configService.getCollectionName()));
            status.setLatencyMs(System.currentTimeMillis() - start);
            return status;
        } catch (RuntimeException e) {
            status.setConnected(false);
            status.setLatencyMs(System.currentTimeMillis() - start);
            status.setMessage(e.getMessage());
            return status;
        }
    }

    public List<String> collections() {
        JsonNode root = sendGet("/collections");
        JsonNode items = root.path("result").path("collections");
        if (!items.isArray()) {
            throw new BizException("Qdrant 集合列表返回格式不支持");
        }
        List<String> collections = new ArrayList<>();
        for (JsonNode item : items) {
            String name = item.path("name").asText(null);
            if (StringUtils.hasText(name)) {
                collections.add(name);
            }
        }
        return collections;
    }

    public void ensureCollection(int vectorSize) {
        if (collections().contains(configService.getCollectionName())) {
            return;
        }
        Map<String, Object> body = Map.of(
                "vectors", Map.of(
                        "size", vectorSize,
                        "distance", "Cosine"));
        send("PUT", "/collections/" + configService.getCollectionName(), body);
    }

    public void upsert(String pointId, List<Double> vector, Map<String, Object> payload) {
        Map<String, Object> body = Map.of("points", List.of(Map.of(
                "id", pointId,
                "vector", vector,
                "payload", payload)));
        send("PUT", "/collections/" + configService.getCollectionName() + "/points?wait=true", body);
    }

    public void deletePoints(List<String> pointIds) {
        if (pointIds == null || pointIds.isEmpty() || !collections().contains(configService.getCollectionName())) {
            return;
        }
        send("POST", "/collections/" + configService.getCollectionName() + "/points/delete?wait=true",
                Map.of("points", pointIds));
    }

    public List<SearchHit> search(List<Double> vector, String knowledgeBaseId, int topK) {
        Map<String, Object> filter = Map.of(
                "must", List.of(Map.of(
                        "key", "knowledgeBaseId",
                        "match", Map.of("value", knowledgeBaseId))));
        Map<String, Object> body = Map.of(
                "vector", vector,
                "limit", topK,
                "filter", filter,
                "with_payload", true);
        JsonNode root = send("POST", "/collections/" + configService.getCollectionName() + "/points/search", body);
        JsonNode result = root.path("result");
        if (!result.isArray()) {
            throw new BizException("Qdrant 召回结果格式不支持");
        }
        List<SearchHit> hits = new ArrayList<>();
        for (JsonNode item : result) {
            hits.add(new SearchHit(item.path("id").asText(), item.path("score").asDouble()));
        }
        return hits;
    }

    private JsonNode sendGet(String path) {
        return send("GET", path, null);
    }

    private JsonNode send(String method, String path, Object body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + path))
                    .timeout(Duration.ofMillis(configService.getQdrantTimeoutMs()));
            if (StringUtils.hasText(configService.getQdrantApiKey())) {
                builder.header("api-key", configService.getQdrantApiKey());
            }
            if (body == null) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                builder.header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
            }
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(configService.getQdrantTimeoutMs()))
                    .build();
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new BizException("Qdrant 请求失败（HTTP " + response.statusCode() + "）：" + response.body());
            }
            return objectMapper.readTree(response.body());
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("Qdrant 连接失败：" + e.getMessage());
        }
    }

    private String baseUrl() {
        String url = configService.getQdrantUrl();
        if (!StringUtils.hasText(url)) {
            throw new BizException("Qdrant URL 不能为空");
        }
        return url.trim().replaceAll("/+$", "");
    }

    public record SearchHit(String pointId, double score) {
    }
}
