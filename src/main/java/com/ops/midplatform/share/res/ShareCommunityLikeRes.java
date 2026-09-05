package com.ops.midplatform.share.res;

import lombok.Data;

/** 社区点赞响应。 */
@Data
public class ShareCommunityLikeRes {
    private String targetType;
    private String targetKey;
    private Long likeCount;
    private Boolean liked;
}
