package com.ops.midplatform.core.tools;

import com.ops.midplatform.core.context.McpUserContextHolder;
import com.ops.midplatform.core.entity.McpRequestConfigEntity;
import com.ops.midplatform.core.mapper.McpRequestConfigMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通过 MCP Tool 创建和查询 API 请求配置。
 *
 * <p>这组工具面向 AI Agent：页面创作是人在浏览器里填表，
 * 这里则让 Agent 通过 tools/call 把 HTTP API 保存成 mcp_request_config。</p>
 */
@Component
public class RequestConfigTools {

    private static final String HTTP_TYPE = "HTTP";
    private static final int STATUS_PRIVATE = 1;

    private final McpRequestConfigMapper requestConfigMapper;
    private final ObjectMapper objectMapper;

    public RequestConfigTools(McpRequestConfigMapper requestConfigMapper, ObjectMapper objectMapper) {
        this.requestConfigMapper = requestConfigMapper;
        this.objectMapper = objectMapper;
    }

    @Tool(name = "create_request_config",
            description = "创建可复用的 HTTP API 能力配置，把外部 HTTP 接口接入运营中台。创建成功后，可在动态 Tool 脚本中通过 runRequest.runRequest(config_key, params) 调用。")
    public Map<String, Object> createRequestConfig(
            @ToolParam(description = "API 能力的唯一调用 key。动态 Tool 脚本会把它作为 runRequest.runRequest 的第一个参数", required = true)
            String config_key,
            @ToolParam(description = "API 名称，面向人展示", required = true)
            String name,
            @ToolParam(description = "HTTP 方法，如 GET、POST、PUT、DELETE", required = true)
            String method,
            @ToolParam(description = "请求 URL，支持 {{param}} 占位符", required = true)
            String url,
            @ToolParam(description = "请求头 JSON 对象，值支持 {{key}} 占位符，如 {\"Authorization\":\"Bearer {{token}}\"}", required = false)
            String headers,
            @ToolParam(description = "请求体模板，支持 {{key}} 占位符，POST/PUT 常用", required = false)
            String body_template,
            @ToolParam(description = "默认参数 JSON 对象，用于调试和占位符默认值", required = false)
            String params_default,
            @ToolParam(description = "API 描述", required = false)
            String description,
            @ToolParam(description = "API 分类", required = false)
            String category,
            @ToolParam(description = "连接超时毫秒，默认 5000", required = false)
            Integer connect_timeout_ms,
            @ToolParam(description = "读取超时毫秒，默认 15000", required = false)
            Integer read_timeout_ms,
            @ToolParam(description = "每分钟限流次数，0 表示不限流", required = false)
            Integer rate_limit_per_minute
    ) {
        Long userId = McpUserContextHolder.getUserId();
        if (userId == null) {
            return failure("请先使用 MCP Token 调用");
        }

        try {
            validateRequired(config_key, "config_key 不能为空");
            validateRequired(name, "name 不能为空");
            validateRequired(method, "method 不能为空");
            validateRequired(url, "url 不能为空");

            String configKey = config_key.trim();
            if (requestConfigMapper.findByConfigKey(configKey) != null) {
                return failure("配置 key 已存在: " + configKey);
            }

            McpRequestConfigEntity entity = new McpRequestConfigEntity();
            entity.setRequestId(nextRequestId());
            entity.setConfigKey(configKey);
            entity.setName(name.trim());
            entity.setType(HTTP_TYPE);
            entity.setMethod(method.trim().toUpperCase());
            entity.setUrl(url.trim());
            entity.setHeaders(normalizeJsonObject(headers));
            entity.setBodyTemplate(body_template != null ? body_template : "");
            entity.setParamsDefault(normalizeJsonObject(params_default));
            entity.setConnectTimeoutMs(connect_timeout_ms != null ? connect_timeout_ms : 5000);
            entity.setReadTimeoutMs(read_timeout_ms != null ? read_timeout_ms : 15000);
            entity.setServiceName(null);
            entity.setMethodName(null);
            entity.setArgsSchema("{}");
            entity.setCreatorId(userId);
            entity.setIsEnabled(1);
            entity.setRateLimitPerMinute(rate_limit_per_minute != null ? rate_limit_per_minute : 0);
            entity.setPublishStatus(STATUS_PRIVATE);
            entity.setDescription(description);
            entity.setCategory(category);

            requestConfigMapper.insert(entity);

            Map<String, Object> result = success("API 请求配置创建成功");
            result.put("id", entity.getId());
            result.put("request_id", entity.getRequestId());
            result.put("config_key", entity.getConfigKey());
            result.put("publish_status", entity.getPublishStatus());
            result.put("next_step", "可在 create_dynamic_tool 中把该 config_key 放入 linked_request_keys");
            return result;
        } catch (Exception e) {
            return failure(e.getMessage());
        }
    }

    @Tool(name = "list_request_configs",
            description = "查询当前用户已接入的 HTTP API 能力配置，用于确认哪些 config_key 可以交给动态 Tool 作为可调用 API 白名单。")
    public Map<String, Object> listRequestConfigs(
            @ToolParam(description = "关键词，可选", required = false)
            String keyword
    ) {
        Long userId = McpUserContextHolder.getUserId();
        if (userId == null) {
            return failure("请先使用 MCP Token 调用");
        }

        String normalizedKeyword = keyword != null ? keyword.trim().toLowerCase() : "";
        List<Map<String, Object>> records = requestConfigMapper.findShareStudioApis(userId)
                .stream()
                .filter(item -> matches(item, normalizedKeyword))
                .map(this::toSimpleMap)
                .toList();

        Map<String, Object> result = success("查询成功");
        result.put("total", records.size());
        result.put("records", records);
        return result;
    }

    private String nextRequestId() {
        long seed = System.currentTimeMillis() % 1_000_000_0000L;
        String requestId = String.format("API%010d", seed);
        while (requestConfigMapper.findByRequestId(requestId) != null) {
            seed++;
            requestId = String.format("API%010d", seed);
        }
        return requestId;
    }

    private boolean matches(McpRequestConfigEntity entity, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        return contains(entity.getConfigKey(), keyword)
                || contains(entity.getName(), keyword)
                || contains(entity.getDescription(), keyword)
                || contains(entity.getUrl(), keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private String normalizeJsonObject(String json) throws Exception {
        if (json == null || json.isBlank()) {
            return "{}";
        }
        objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });
        return json;
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private Map<String, Object> toSimpleMap(McpRequestConfigEntity entity) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", entity.getId());
        item.put("request_id", entity.getRequestId());
        item.put("config_key", entity.getConfigKey());
        item.put("name", entity.getName());
        item.put("method", entity.getMethod());
        item.put("url", entity.getUrl());
        item.put("description", entity.getDescription());
        item.put("publish_status", entity.getPublishStatus());
        item.put("is_enabled", entity.getIsEnabled());
        return item;
    }

    private Map<String, Object> success(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", message);
        return result;
    }

    private Map<String, Object> failure(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}
