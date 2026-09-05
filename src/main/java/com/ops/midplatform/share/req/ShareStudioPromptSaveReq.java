package com.ops.midplatform.share.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 创作空间保存 Prompt 模板请求。 */
@Data
public class ShareStudioPromptSaveReq {
    /** MCP Prompt 名称。 */
    @NotBlank(message = "Prompt名称不能为空")
    private String promptName;

    /** 展示标题。 */
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 描述。 */
    private String description;

    /** MCP Prompt arguments JSON 数组。 */
    private String argumentsSchema;

    /** 模板正文，支持 {{name}} 占位符。 */
    @NotBlank(message = "模板内容不能为空")
    private String templateContent;

    /** 建议使用的工具名 JSON 数组。 */
    private String linkedToolNames;

    /** 是否启用。 */
    private Integer enabled;

    /** 发布状态：0-草稿，1-已上线不公开，2-已上线公开。 */
    private Integer publishStatus;
}
