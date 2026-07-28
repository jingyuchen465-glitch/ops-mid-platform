package com.bear.mcp.single.admin.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Token 工具选择明细请求参数。 */
@Data
public class AdminTokenSelectionItemReq {
    /** MCP 工具名称。 */
    @NotBlank(message = "工具名称不能为空")
    private String toolName;

    /** 工具类型：BUILTIN 或 DYNAMIC。 */
    @NotBlank(message = "工具类型不能为空")
    private String toolType;

    /** 是否启用：1-启用，0-禁用。 */
    private Integer enabled;
}
