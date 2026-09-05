package com.ops.midplatform.core.dynamic;

/**
 * 返回给 tools/list 的工具信息。
 *
 * <p>内置工具和动态工具最终都会转换成这个统一结构。</p>
 */
public record ToolInfo(
        /** MCP 工具名。 */
        String name,
        /** 工具描述。 */
        String description,
        /** 工具入参 schema。 */
        String inputSchema,
        /** 工具类型：BUILTIN 或 DYNAMIC。 */
        String type
) {
}
