package com.bear.mcp.single.core.entity;

import lombok.Data;

import java.util.Date;

/** 对应 mcp_role_tool 表。 */
@Data
public class McpRoleToolEntity {
    /** 主键。 */
    private Long id;
    /** 角色编码。 */
    private String roleCode;
    /** MCP 工具名称。 */
    private String toolName;
    /** 创建时间。 */
    private Date createTime;
}
