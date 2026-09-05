package com.bear.mcp.single.admin.res;

import lombok.Data;

/** 管理后台 Prompt 权限配置中的可选 Prompt。 */
@Data
public class AdminPromptTemplateRes {
    private Long id;
    private String promptName;
    private String title;
    private String description;
    private Integer enabled;
    private Integer publishStatus;
}
