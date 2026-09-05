package com.bear.mcp.single.core.entity;

import lombok.Data;

import java.util.Date;

/** 对应 mcp_audit_log 表。 */
@Data
public class McpAuditLogEntity {
    /** 主键。 */
    private Long id;
    /** 创建时间。 */
    private Date createTime;
    /** 调用用户 ID。 */
    private Long userId;
    /** 调用用户名。 */
    private String userName;
    /** MCP 工具名称。 */
    private String toolName;
    /** 请求参数摘要。 */
    private String requestParams;
    /** 响应内容摘要。 */
    private String responseSummary;
    /** 调用状态。 */
    private String status;
    /** 错误信息。 */
    private String errorMessage;
    /** 调用耗时，单位毫秒。 */
    private Long durationMs;
}
