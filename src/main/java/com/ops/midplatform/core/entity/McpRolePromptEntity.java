package com.bear.mcp.single.core.entity;

import lombok.Data;

import java.util.Date;

/** 对应 mcp_role_prompt 表。 */
@Data
public class McpRolePromptEntity {
    /** 主键。 */
    private Long id;
    /** 角色编码。 */
    private String roleCode;
    /** MCP Prompt 名称。 */
    private String promptName;
    /** 创建时间。 */
    private Date createTime;
}
