package com.bear.mcp.single.core.entity;

import lombok.Data;

/** 对应 mcp_user_tool_selection 表。 */
@Data
public class McpUserToolSelectionEntity {
    /** 主键。 */
    private Long id;
    /** Token ID。 */
    private Long tokenId;
    /** MCP 工具名称。 */
    private String toolName;
    /** 工具类型：BUILTIN 或 DYNAMIC。 */
    private String toolType;
    /** 是否启用：1-启用，0-禁用。 */
    private Integer enabled;
}
