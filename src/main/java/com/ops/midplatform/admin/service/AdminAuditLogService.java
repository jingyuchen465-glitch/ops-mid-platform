package com.ops.midplatform.admin.service;

import com.ops.midplatform.admin.res.AdminAuditLogRes;
import com.ops.midplatform.core.entity.McpAuditLogEntity;
import com.ops.midplatform.core.mapper.McpAuditLogMapper;
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
        res.setCapabilityType(capabilityTypeOf(entity.getToolName()));
        res.setCapabilityName(capabilityNameOf(entity.getToolName()));
        res.setCapabilityAction(capabilityActionOf(entity.getToolName(), entity.getRequestParams()));
        res.setRequestParams(entity.getRequestParams());
        res.setResponseSummary(entity.getResponseSummary());
        res.setStatus(entity.getStatus());
        res.setErrorMessage(entity.getErrorMessage());
        res.setDurationMs(entity.getDurationMs());
        return res;
    }

    private String capabilityTypeOf(String toolName) {
        if (toolName != null && toolName.startsWith("PROMPT:")) {
            return "PROMPT";
        }
        if (toolName != null && toolName.startsWith("RESOURCE:")) {
            return "RESOURCE";
        }
        return "TOOL";
    }

    private String capabilityNameOf(String toolName) {
        if (toolName == null || toolName.isBlank()) {
            return "";
        }
        if (toolName.startsWith("PROMPT:")) {
            return toolName.substring("PROMPT:".length());
        }
        if (toolName.startsWith("RESOURCE:")) {
            return toolName.substring("RESOURCE:".length());
        }
        return toolName;
    }

    private String capabilityActionOf(String toolName, String requestParams) {
        String type = capabilityTypeOf(toolName);
        String request = requestParams == null ? "" : requestParams;
        if ("PROMPT".equals(type)) {
            return request.contains("prompts/list") ? "LIST" : "GET";
        }
        if ("RESOURCE".equals(type)) {
            return request.contains("resources/list") ? "LIST" : "READ";
        }
        return "CALL";
    }
}
