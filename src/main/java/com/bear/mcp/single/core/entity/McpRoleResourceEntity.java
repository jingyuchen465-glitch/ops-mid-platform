package com.bear.mcp.single.core.entity;

import lombok.Data;

import java.util.Date;

/** 对应 mcp_role_resource 表。 */
@Data
public class McpRoleResourceEntity {
    private Long id;
    private String roleCode;
    private String resourceUri;
    private Date createTime;
}
