package com.bear.mcp.single.core.request;

import com.bear.mcp.single.core.entity.McpRequestConfigEntity;
import com.bear.mcp.single.core.mapper.McpRequestConfigMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RequestConfigService {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}]+)}}");
    private static final ConcurrentHashMap<String, AtomicInteger> RATE_COUNTERS = new ConcurrentHashMap<>();

    private final McpRequestConfigMapper requestConfigMapper;
    private final ObjectMapper objectMapper;

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
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(config.connectTimeoutMs() != null ? config.connectTimeoutMs() : 5000);
            connection.setReadTimeout(config.readTimeoutMs() != null ? config.readTimeoutMs() : 15000);
            for (Map.Entry<String, String> header : config.headers().entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
            if (!"GET".equals(method) && !"HEAD".equals(method)) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write((body != null ? body : "{}").getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = connection.getResponseCode();
            var stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
            String response = "";
            if (stream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append('\n');
                    }
                    response = builder.toString().trim();
                }
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", status);
            result.put("body", response);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("HTTP 请求失败: " + e.getMessage(), e);
        }
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
