package com.bear.mcp.single.share.res;

import lombok.Data;

/**
 * 创作空间动态 Tool 响应。
 */
@Data
public class ShareStudioToolRes {

    /**
     * 主键。
     */
    private Long id;

    /**
     * MCP Tool 名称。
     */
    private String toolName;

    /**
     * MCP Tool 描述。
     */
    private String toolDescription;

    /**
     * MCP inputSchema JSON。
     */
    private String inputSchema;

    /**
     * Groovy 脚本。
     */
    private String groovyScript;

    /**
     * 允许 runRequest 调用的 API 配置 key 列表 JSON。
     */
    private String linkedRequestKeys;

    /**
     * 允许 runSql 查询的数据源 id 列表 JSON。
     */
    private String linkedDataSourceIds;

    /**
     * 允许脚本访问的Redis key、命令和字段权限 JSON。
     */
    private String linkedRedisPermissions;

    /**
     * 是否启用：1-启用，0-禁用。
     */
    private Integer enabled;

    /**
     * 发布状态：0-草稿，1-已上线不公开，2-已上线公开。
     */
    private Integer publishStatus;
}
