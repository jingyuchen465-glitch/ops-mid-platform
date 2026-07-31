package com.bear.mcp.single.share.res;

import lombok.Data;

import java.util.Date;

/** 创作空间 Resource 响应。 */
@Data
public class ShareStudioResourceRes {
    private Long id;
    private String resourceUri;
    private String name;
    private String description;
    private String mimeType;
    private String objectKey;
    private String fileName;
    private Long fileSize;
    private Long creatorId;
    private Integer enabled;
    private Integer publishStatus;
    private Date createTime;
    private Date updateTime;
}
