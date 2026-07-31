package com.bear.mcp.single.share.res;

import lombok.Data;

import java.util.Date;

/** 创作空间 Skill 响应。 */
@Data
public class ShareStudioSkillRes {
    private Long id;
    private String skillCode;
    private String name;
    private String description;
    private String category;
    private String objectKey;
    private String fileName;
    private Long fileSize;
    private Long creatorId;
    private Integer enabled;
    private Integer publishStatus;
    private Date createTime;
    private Date updateTime;
}
