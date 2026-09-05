package com.ops.midplatform.core.dynamic;

import java.util.List;

import com.ops.midplatform.core.redis.RedisPermission;

/**
 * 动态工具的运行时对象。
 *
 * <p>它由 mcp_dynamic_tool 表转换而来，供 tools/list 展示和 tools/call 执行使用。</p>
 */
public record DynamicTool(
        /** MCP 工具名。 */
        String name,
        /** 工具描述，会影响 AI Client 选择哪个工具。 */
        String description,
        /** MCP Tool inputSchema，描述工具入参。 */
        String inputSchema,
        /** 动态工具真正执行的 Groovy 脚本。 */
        String script,
        /** 脚本允许调用的请求配置 key 白名单。 */
        List<String> linkedRequestKeys,
        /** 脚本允许访问的数据源 id 白名单。 */
        List<Long> linkedDataSourceIds,
        /** 脚本允许访问的Redis权限。 */
        List<RedisPermission> linkedRedisPermissions,
        /** 工具是否启用。 */
        boolean enabled
) {
}
