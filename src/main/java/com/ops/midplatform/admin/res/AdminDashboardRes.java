package com.bear.mcp.single.admin.res;

import lombok.Data;

import java.util.List;

/** 管理首页返回对象。 */
@Data
public class AdminDashboardRes {
    /** 用户总数。 */
    private int users;

    /** 角色总数。 */
    private int roles;

    /** 启用中的 Token 数。 */
    private int activeTokens;

    /** 启用中的请求配置数。 */
    private int enabledRequests;

    /** 启用中的动态工具数。 */
    private int enabledDynamicTools;

    /** 今日 MCP 工具调用次数。 */
    private int todayCalls;

    /** 最近审计日志。 */
    private List<AdminAuditLogRes> recentAudits;
}
