package com.bear.mcp.single.core.request;

import com.bear.mcp.single.core.entity.McpRequestConfigEntity;
import com.bear.mcp.single.core.mapper.McpRequestConfigMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RequestConfigService {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}]+)}}");
    private static final ConcurrentHashMap<String, AtomicInteger> RATE_COUNTERS = new ConcurrentHashMap<>();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final McpRequestConfigMapper requestConfigMapper;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient = new OkHttpClient();

    public RequestConfigService(McpRequestConfigMapper requestConfigMapper, ObjectMapper objectMapper) {
        this.requestConfigMapper = requestConfigMapper;
        this.objectMapper = objectMapper;
    }

    public Object execute(String key, Map<String, Object> params) {
        RequestConfig config = findByKey(key);
        if (config == null) {
            throw new IllegalArgumentException("请求配置不存在: " + key);
        }
        if (!config.enabled()) {
            throw new IllegalArgumentException("请求配置已禁用: " + key);
        }
        Map<String, Object> finalParams = mergeParams(config.paramsDefault(), params);
        checkRateLimit(config);
        if ("MOCK".equalsIgnoreCase(config.type()) || "MOCK".equalsIgnoreCase(config.method())) {
            return Map.of("key", key, "now", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), "params", finalParams);
        }
        if (!"HTTP".equalsIgnoreCase(config.type())) {
            throw new IllegalArgumentException("当前单体版未接入 " + config.type()
                    + " 客户端，请先配置 HTTP 类型或补充对应协议客户端");
        }
        return executeHttp(config, finalParams);
    }

    private RequestConfig findByKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        McpRequestConfigEntity entity = requestConfigMapper.findByConfigKey(key);
        if (entity == null) {
            return null;
        }
        return new RequestConfig(
                entity.getRequestId(),
                entity.getConfigKey(),
                entity.getName(),
                entity.getType(),
                entity.getMethod(),
                entity.getUrl(),
                parseHeaders(entity.getHeaders()),
                entity.getBodyTemplate(),
                parseParams(entity.getParamsDefault()),
                entity.getConnectTimeoutMs(),
                entity.getReadTimeoutMs(),
                entity.getServiceName(),
                entity.getMethodName(),
                entity.getArgsSchema(),
                entity.getCreatorId(),
                entity.getRateLimitPerMinute(),
                entity.getPublishStatus(),
                entity.getDescription(),
                entity.getCategory(),
                Integer.valueOf(1).equals(entity.getIsEnabled())
        );
    }

    private Object executeHttp(RequestConfig config, Map<String, Object> params) {
        try {
            String method = config.method() != null ? config.method().toUpperCase() : "GET";
            String url = replace(config.url(), params);
            String body = replace(config.bodyTemplate(), params);

            OkHttpClient client = httpClient.newBuilder()
                    .connectTimeout(config.connectTimeoutMs() != null ? config.connectTimeoutMs() : 5000, TimeUnit.MILLISECONDS)
                    .readTimeout(config.readTimeoutMs() != null ? config.readTimeoutMs() : 15000, TimeUnit.MILLISECONDS)
                    .build();

            Request.Builder requestBuilder = new Request.Builder().url(url);
            for (Map.Entry<String, String> header : config.headers().entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }

            RequestBody requestBody = null;
            if (requiresRequestBody(method)) {
                requestBody = RequestBody.create(body != null ? body : "{}", JSON_MEDIA_TYPE);
                if (!hasHeader(config.headers(), "Content-Type")) {
                    requestBuilder.header("Content-Type", "application/json; charset=UTF-8");
                }
            }

            Request request = requestBuilder.method(method, requestBody).build();

            try (Response response = client.newCall(request).execute()) {
                ResponseBody responseBody = response.body();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", response.code());
                result.put("body", responseBody != null ? responseBody.string().trim() : "");
                return result;
            }
        } catch (Exception e) {
            throw new RuntimeException("HTTP 请求失败: " + e.getMessage(), e);
        }
    }

    private boolean requiresRequestBody(String method) {
        return !"GET".equals(method) && !"HEAD".equals(method);
    }

    private boolean hasHeader(Map<String, String> headers, String name) {
        if (headers == null || headers.isEmpty()) {
            return false;
        }
        for (String headerName : headers.keySet()) {
            if (headerName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private String replace(String template, Map<String, Object> params) {
        if (template == null) {
            return null;
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object value = params.get(key);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(value != null ? value.toString() : ""));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private Map<String, String> parseHeaders(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return Map.of();
        }
    }

    private Map<String, Object> parseParams(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return Map.of();
        }
    }

    private Map<String, Object> mergeParams(Map<String, Object> defaults, Map<String, Object> params) {
        Map<String, Object> merged = new HashMap<>();
        if (defaults != null) {
            merged.putAll(defaults);
        }
        if (params != null) {
            merged.putAll(params);
        }
        return merged;
    }

    private void checkRateLimit(RequestConfig config) {
        int limit = config.rateLimitPerMinute() != null ? config.rateLimitPerMinute() : 0;
        if (limit <= 0) {
            return;
        }
        String minute = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        String key = config.key() + ":" + minute;
        int current = RATE_COUNTERS.computeIfAbsent(key, ignored -> new AtomicInteger()).incrementAndGet();
        if (current > limit) {
            throw new IllegalStateException("请求配置触发限流: " + config.key());
        }
    }
}
