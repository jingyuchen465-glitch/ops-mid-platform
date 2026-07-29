package com.bear.mcp.single.admin.req;

import lombok.Data;

/** Token Prompt 选择项。 */
@Data
public class AdminTokenPromptSelectionItemReq {
    /** MCP Prompt 名称。 */
    private String promptName;
    /** 是否启用：1-启用，0-禁用。 */
    private Integer enabled;
}
