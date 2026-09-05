package com.ops.midplatform.admin.service;

import com.ops.midplatform.admin.res.AdminDashboardRes;
import com.ops.midplatform.core.mapper.McpAuditLogMapper;
import com.ops.midplatform.core.mapper.McpDynamicToolMapper;
import com.ops.midplatform.core.mapper.McpRequestConfigMapper;
import com.ops.midplatform.core.mapper.McpRoleMapper;
import com.ops.midplatform.core.mapper.McpUserMapper;
import com.ops.midplatform.core.mapper.McpUserTokenMapper;
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
