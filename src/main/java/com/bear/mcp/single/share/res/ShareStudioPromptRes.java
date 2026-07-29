package com.bear.mcp.single.share.res;

import lombok.Data;

/** 创作空间 Prompt 模板响应。 */
@Data
public class ShareStudioPromptRes {
    private Long id;
    private String promptName;
    private String title;
    private String description;
    private String argumentsSchema;
    private String templateContent;
    private String linkedToolNames;
    private Long creatorId;
    private Integer enabled;
    private Integer publishStatus;
}
