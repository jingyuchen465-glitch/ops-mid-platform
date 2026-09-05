package com.ops.midplatform.core.entity;

import lombok.Data;

import java.util.Date;

/** 对应 mcp_community_like 表。 */
@Data
public class McpCommunityLikeEntity {
    private Long id;
    private String targetType;
    private String targetKey;
    private Long userId;
    private Date createTime;
}
