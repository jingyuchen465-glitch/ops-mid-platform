package com.bear.mcp.single.request;

import java.util.Map;

public record RequestConfig(
        String key,
        String name,
        String method,
        String url,
        Map<String, String> headers,
        String bodyTemplate,
        boolean enabled
) {
}
