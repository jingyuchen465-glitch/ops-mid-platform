package com.bear.mcp.single.admin.service;

import com.bear.mcp.single.admin.res.AdminDashboardRes;
import com.bear.mcp.single.core.mapper.McpAuditLogMapper;
import com.bear.mcp.single.core.mapper.McpDynamicToolMapper;
import com.bear.mcp.single.core.mapper.McpRequestConfigMapper;
import com.bear.mcp.single.core.mapper.McpRoleMapper;
import com.bear.mcp.single.core.mapper.McpUserMapper;
import com.bear.mcp.single.core.mapper.McpUserTokenMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/** 管理首页的跨表统计编排。 */
@Service
public class AdminDashboardService {
    private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");

    private final McpUserMapper userMapper;
    private final McpRoleMapper roleMapper;
    private final McpUserTokenMapper tokenMapper;
    private final McpRequestConfigMapper requestConfigMapper;
    private final McpDynamicToolMapper dynamicToolMapper;
    private final McpAuditLogMapper auditLogMapper;
    private final AdminAuditLogService auditLogService;

    public AdminDashboardService(McpUserMapper userMapper, McpRoleMapper roleMapper,
                                 McpUserTokenMapper tokenMapper, McpRequestConfigMapper requestConfigMapper,
                                 McpDynamicToolMapper dynamicToolMapper, McpAuditLogMapper auditLogMapper,
                                 AdminAuditLogService auditLogService) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.tokenMapper = tokenMapper;
        this.requestConfigMapper = requestConfigMapper;
        this.dynamicToolMapper = dynamicToolMapper;
        this.auditLogMapper = auditLogMapper;
        this.auditLogService = auditLogService;
    }

    public AdminDashboardRes getDashboard() {
        AdminDashboardRes res = new AdminDashboardRes();
        res.setUsers(userMapper.countAll());
        res.setRoles(roleMapper.countAll());
        res.setActiveTokens(tokenMapper.countActive());
        res.setEnabledRequests(requestConfigMapper.countEnabled());
        res.setEnabledDynamicTools(dynamicToolMapper.countEnabled());
        res.setTodayCalls(auditLogMapper.countBetween(todayStart(), tomorrowStart()));
        res.setRecentAudits(auditLogMapper.findRecent().stream().map(auditLogService::toRes).toList());
        return res;
    }

    private Date todayStart() {
        return Date.from(LocalDate.now(CHINA_ZONE).atStartOfDay(CHINA_ZONE).toInstant());
    }

    private Date tomorrowStart() {
        return Date.from(LocalDate.now(CHINA_ZONE).plusDays(1).atStartOfDay(CHINA_ZONE).toInstant());
    }
}
