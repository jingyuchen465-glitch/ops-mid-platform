package com.bear.mcp.single.dynamic;

import java.util.List;

public record DynamicTool(
        String name,
        String description,
        String inputSchema,
        String script,
        List<String> linkedRequestKeys,
        boolean enabled
) {
}
