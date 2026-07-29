package com.bear.mcp.single.admin.res;

import lombok.Data;

/** 管理端动态工具返回对象。 */
@Data
public class AdminDynamicToolRes {
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

    /** 发布状态：0-草稿，1-已上线不公开，2-已上线公开。 */
    private Integer publishStatus;
}
