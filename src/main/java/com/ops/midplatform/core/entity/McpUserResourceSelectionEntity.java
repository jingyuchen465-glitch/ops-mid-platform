package com.ops.midplatform.core.entity;

import lombok.Data;

/** 对应 mcp_user_resource_selection 表。 */
@Data
public class McpUserResourceSelectionEntity {
    private Long id;
    private Long tokenId;
    private String resourceUri;
    private Integer enabled;
}
