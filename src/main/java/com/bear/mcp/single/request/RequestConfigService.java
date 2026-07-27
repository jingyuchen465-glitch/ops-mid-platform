package com.bear.mcp.single.request;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RequestConfigService {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}]+)}}");

    private final Map<String, RequestConfig> configs = new ConcurrentHashMap<>();

    public RequestConfigService() {
        configs.put("demo_clock", new RequestConfig(
                "demo_clock",
                "Demo clock",
                "MOCK",
                "",
                Map.of(),
                "",
                true
        ));
    }

    public Object execute(String key, Map<String, Object> params) {
        RequestConfig config = configs.get(key);
        if (config == null) {
            throw new IllegalArgumentException("请求配置不存在: " + key);
        }
        if (!config.enabled()) {
            throw new IllegalArgumentException("请求配置已禁用: " + key);
        }
        if ("MOCK".equalsIgnoreCase(config.method())) {
            return Map.of("key", key, "now", LocalDateTime.now().toString(), "params", params != null ? params : Map.of());
        }
        return executeHttp(config, params != null ? params : Map.of());
    }

    private Object executeHttp(RequestConfig config, Map<String, Object> params) {
        try {
            String method = config.method() != null ? config.method().toUpperCase() : "GET";
            String url = replace(config.url(), params);
            String body = replace(config.bodyTemplate(), params);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
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
}
