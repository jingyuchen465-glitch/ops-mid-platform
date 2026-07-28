package com.bear.mcp.single.admin.res;

import lombok.Data;

/** 管理端 Token 工具选择返回对象。 */
@Data
public class AdminTokenSelectionRes {
    /** 主键。 */
    private Long id;

    /** Token ID。 */
    private Long tokenId;

    /** MCP 工具名称。 */
    private String toolName;

    /** 工具类型：BUILTIN 或 DYNAMIC。 */
    private String toolType;

    /** 是否启用：1-启用，0-禁用。 */
    private Integer enabled;
}
