package com.bear.mcp.single.admin.service;

import com.bear.mcp.single.admin.res.AdminAuditLogRes;
import com.bear.mcp.single.core.entity.McpAuditLogEntity;
import com.bear.mcp.single.core.mapper.McpAuditLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAuditLogService {
    private final McpAuditLogMapper mapper;

    public AdminAuditLogService(McpAuditLogMapper mapper) {
        this.mapper = mapper;
    }

    public List<AdminAuditLogRes> listRecent() {
        return mapper.findRecent().stream().map(this::toRes).toList();
    }

    public AdminAuditLogRes toRes(McpAuditLogEntity entity) {
        AdminAuditLogRes res = new AdminAuditLogRes();
        res.setId(entity.getId());
        res.setCreateTime(entity.getCreateTime());
        res.setUserId(entity.getUserId());
        res.setUserName(entity.getUserName());
        res.setToolName(entity.getToolName());
        res.setRequestParams(entity.getRequestParams());
        res.setResponseSummary(entity.getResponseSummary());
        res.setStatus(entity.getStatus());
        res.setErrorMessage(entity.getErrorMessage());
        res.setDurationMs(entity.getDurationMs());
        return res;
    }
}
