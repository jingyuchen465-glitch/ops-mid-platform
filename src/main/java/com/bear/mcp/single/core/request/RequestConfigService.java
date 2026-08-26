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

    /**
     * 匹配模板里的 {{变量名}}。
     * 例如 URL /user/{{userId}} 会从 params 里取 userId 替换进去。
     */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}]+)}}");

    /**
     * 课堂版内存限流计数器。
     * key 格式为 configKey:yyyyMMddHHmm，只做单机演示，生产环境应放到 Redis。
     */
    private static final ConcurrentHashMap<String, AtomicInteger> RATE_COUNTERS = new ConcurrentHashMap<>();

    /**
     * 默认按 JSON 请求体发送。
     */
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    /**
     * mcp_request_config：动态工具可引用的企业请求配置表。
     */
    private final McpRequestConfigMapper requestConfigMapper;

    /**
     * 解析 headers、params_default 这些 JSON 字段。
     */
    private final ObjectMapper objectMapper;

    /**
     * OkHttpClient 是线程安全的基础客户端。
     * 每次请求会基于它派生带不同超时时间的 client。
     */
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

        /*
         * params_default 是配置层的默认参数，params 是脚本运行时传入参数。
         * 运行时参数优先级更高，所以后 merge 的 params 会覆盖默认值。
         */
        Map<String, Object> finalParams = mergeParams(config.paramsDefault(), params);

        checkRateLimit(config);

        if (!"HTTP".equalsIgnoreCase(config.type())) {
            throw new IllegalArgumentException("当前单体版未接入 " + config.type()
                    + " 客户端，请先配置 HTTP 类型或补充对应协议客户端");
        }

        return executeHttp(config, finalParams);
    }

    /**
     * 执行一份还没有落库的临时 HTTP API 配置。
     *
     * <p>创作空间点击“发送”时使用这个方法：先验证当前表单能不能真实请求成功，
     * 调试通过以后，用户再决定是否保存到 mcp_request_config。</p>
     */
    public Object executeTemporary(McpRequestConfigEntity entity, Map<String, Object> params) {
        RequestConfig config = toRequestConfig(entity);
        if (!config.enabled()) {
            throw new IllegalArgumentException("请求配置已禁用: " + config.key());
        }

        Map<String, Object> finalParams = mergeParams(config.paramsDefault(), params);

        if (!"HTTP".equalsIgnoreCase(config.type())) {
            throw new IllegalArgumentException("当前单体版未接入 " + config.type()
                    + " 客户端，请先配置 HTTP 类型或补充对应协议客户端");
        }

        return executeHttp(config, finalParams);
    }

    /**
     * 根据 config_key 查询请求配置，并转换成运行时 RequestConfig。
     */
    private RequestConfig findByKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        McpRequestConfigEntity entity = requestConfigMapper.findByConfigKey(key);
        if (entity == null) {
            return null;
        }

        return toRequestConfig(entity);
    }

    private RequestConfig toRequestConfig(McpRequestConfigEntity entity) {
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

    /**
     * 执行 HTTP 类型请求配置。
     *
     * <p>这里不再使用 HttpURLConnection，而是用 OkHttp 组织请求。
     * 返回值保持课堂版结构：status + body。</p>
     */
    private Object executeHttp(RequestConfig config, Map<String, Object> params) {
        try {
            String method = config.method() != null ? config.method().toUpperCase() : "GET";

            /*
             * URL 和 bodyTemplate 都允许使用 {{参数名}} 占位符。
             */
            String url = replace(config.url(), params);
            String body = replace(config.bodyTemplate(), params);

            /*
             * 不同请求配置可能有不同超时时间。
             * OkHttpClient 本身可复用，这里基于基础 client 派生带超时设置的新 client。
             */
            OkHttpClient client = httpClient.newBuilder()
                    .connectTimeout(config.connectTimeoutMs() != null ? config.connectTimeoutMs() : 5000, TimeUnit.MILLISECONDS)
                    .readTimeout(config.readTimeoutMs() != null ? config.readTimeoutMs() : 15000, TimeUnit.MILLISECONDS)
                    .build();

            Request.Builder requestBuilder = new Request.Builder().url(url);

            /*
             * 先设置数据库配置中的请求头，请求头的值同样支持 {{参数名}} 占位符。
             * 例如 Authorization: Bearer {{token}}，token 由脚本运行时动态传入。
             */
            for (Map.Entry<String, String> header : config.headers().entrySet()) {
                requestBuilder.header(header.getKey(), replace(header.getValue(), params));
            }

            RequestBody requestBody = null;
            if (requiresRequestBody(method)) {
                requestBody = RequestBody.create(body != null ? body : "{}", JSON_MEDIA_TYPE);

                /*
                 * 如果配置里没有显式 Content-Type，默认按 JSON 发送。
                 */
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

    /**
     * GET 和 HEAD 按 HTTP 语义不发送请求体。
     */
    private boolean requiresRequestBody(String method) {
        return !"GET".equals(method) && !"HEAD".equals(method);
    }

    /**
     * 判断配置中是否已经设置了某个请求头，忽略大小写。
     */
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

    /**
     * 将模板中的 {{参数名}} 替换成 params 中的值。
     */
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

    /**
     * 将 headers JSON 转成 Map。
     * 配置为空或格式错误时返回空 Map，避免因为展示数据影响运行链路。
     */
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

    /**
     * 将 params_default JSON 转成 Map。
     */
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

    /**
     * 合并默认参数和运行时参数。
     */
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

    /**
     * 简单的每分钟限流。
     */
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
