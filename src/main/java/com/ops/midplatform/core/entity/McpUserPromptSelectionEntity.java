package com.bear.mcp.single.core.entity;

import lombok.Data;

/** 对应 mcp_user_prompt_selection 表。 */
@Data
public class McpUserPromptSelectionEntity {
    /** 主键。 */
    private Long id;
    /** Token ID。 */
    private Long tokenId;
    /** MCP Prompt 名称。 */
    private String promptName;
    /** 是否启用：1-启用，0-禁用。 */
    private Integer enabled;
}
