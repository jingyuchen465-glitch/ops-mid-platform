package com.bear.mcp.single.share.req;

import lombok.Data;

/** 社区点赞请求。 */
@Data
public class ShareCommunityLikeReq {
    private String targetType;
    private String targetKey;
}
