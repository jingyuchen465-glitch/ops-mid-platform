package com.bear.mcp.single.core.context;

import java.util.Set;

public record McpUserContext(
        Long userId,
        Long tokenId,
        String userName,
        Set<String> roles,
        Set<String> allowedTools,
        String sessionId,
        String clientIp
) {
}
