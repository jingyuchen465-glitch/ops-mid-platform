package com.ops.midplatform.core.entity;

import lombok.Data;

/** 社区点赞统计结果。 */
@Data
public class McpCommunityLikeStatEntity {
    private String targetType;
    private String targetKey;
    private Long likeCount;
}
