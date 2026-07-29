package com.bear.mcp.single.share.res;

import lombok.Data;

/**
 * 社区公开 MCP Tool 展示响应。
 */
@Data
public class ShareCommunityToolRes {

    /**
     * 数据库主键。
     */
    private Long id;

    /**
     * MCP Tool 名称。
     */
    private String toolName;

    /**
     * Tool 类型：BUILTIN-内置工具，DYNAMIC-动态工具。
     */
    private String toolType;

    /**
     * Tool 分类。
     */
    private String category;

    /**
     * 工具实现语言或实现方式。
     */
    private String scriptLanguage;

    /**
     * MCP Tool 描述。
     */
    private String toolDescription;

    /**
     * MCP inputSchema JSON。
     */
    private String inputSchema;

    /**
     * 动态 Tool 的 Groovy 脚本。
     *
     * <p>内置工具没有 Groovy 脚本，返回空。</p>
     */
    private String groovyScript;

    /**
     * 允许脚本调用的 API 配置 Key 列表 JSON。
     */
    private String linkedRequestKeys;

    /**
     * 允许脚本访问的数据源 id 列表 JSON。
     */
    private String linkedDataSourceIds;

    /**
     * 发布状态：社区只返回 2。
     */
    private Integer publishStatus;
}
