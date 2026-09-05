package com.ops.midplatform.core.entity;

import lombok.Data;

import java.util.Date;

/** 对应 mcp_prompt_template 表，保存创作空间里的 MCP Prompt 模板。 */
@Data
public class McpPromptTemplateEntity {
    /** 主键。 */
    private Long id;
    /** MCP Prompt 名称，全局唯一。 */
    private String promptName;
    /** 给人看的标题。 */
    private String title;
    /** Prompt 描述。 */
    private String description;
    /** MCP Prompt arguments JSON。 */
    private String argumentsSchema;
    /** 模板正文，支持 {{name}} 占位符。 */
    private String templateContent;
    /** 建议使用的工具名 JSON 数组。 */
    private String linkedToolNames;
    /** 创建人。 */
    private Long creatorId;
    /** 是否启用：1-启用，0-禁用。 */
    private Integer enabled;
    /** 发布状态：0-草稿，1-已上线不公开，2-已上线公开。 */
    private Integer publishStatus;
    /** 创建时间。 */
    private Date createTime;
    /** 更新时间。 */
    private Date updateTime;
}
