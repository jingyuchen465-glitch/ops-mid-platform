package com.bear.mcp.single.core.entity;

import lombok.Data;

import java.util.Date;

/** 对应 mcp_skill 表。 */
@Data
public class McpSkillEntity {
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
    /** 发布状态：0-草稿，1-已上线不公开，2-已上线公开。 */
    private Integer publishStatus;
    private Date createTime;
    private Date updateTime;
}
