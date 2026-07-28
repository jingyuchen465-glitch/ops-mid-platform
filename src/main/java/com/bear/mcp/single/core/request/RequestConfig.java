package com.bear.mcp.single.core.request;

import java.util.Map;

/** 运行时请求配置，不对应单张数据库表，来自 McpRequestConfigEntity 的转换。 */
public record RequestConfig(
        String requestId,
        String key,
        String name,
        String type,
        String method,
        String url,
        Map<String, String> headers,
        String bodyTemplate,
        Map<String, Object> paramsDefault,
        Integer connectTimeoutMs,
        Integer readTimeoutMs,
        String serviceName,
        String methodName,
        String argsSchema,
        Long creatorId,
        Integer rateLimitPerMinute,
        Integer publishStatus,
        String description,
        String category,
        boolean enabled
) {
}
