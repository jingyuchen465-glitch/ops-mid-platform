package com.bear.mcp.single.groovy;

import java.util.List;
import java.util.Map;

public record ScriptContext(
        Map<String, Object> params,
        Long userId,
        String userName,
        String toolName,
        List<String> linkedRequestKeys,
        long timeoutMs
) {
}
