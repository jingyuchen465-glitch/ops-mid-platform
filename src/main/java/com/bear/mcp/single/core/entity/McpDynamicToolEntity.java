package com.bear.mcp.single.core.entity;

import lombok.Data;

/** 对应 mcp_dynamic_tool 表。 */
@Data
public class McpDynamicToolEntity {
    /** 主键。 */
    private Long id;
    /** MCP 工具名称。 */
    private String toolName;
    /** 工具描述。 */
    private String toolDescription;
    /** MCP inputSchema JSON。 */
    private String inputSchema;
    /** 动态工具 Groovy 脚本。 */
    private String groovyScript;
    /** 允许调用的请求配置 key 列表 JSON。 */
    private String linkedRequestKeys;
    /** 是否启用：1-启用，0-禁用。 */
    private Integer enabled;
}
