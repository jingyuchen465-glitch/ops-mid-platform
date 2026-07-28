package com.bear.mcp.single.core.audit;

import com.bear.mcp.single.core.entity.McpAuditLogEntity;
import com.bear.mcp.single.core.mapper.McpAuditLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final McpAuditLogMapper auditLogMapper;

    public AuditLogService(McpAuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public void record(AuditLog auditLog) {
        McpAuditLogEntity entity = new McpAuditLogEntity();
        entity.setCreateTime(auditLog.at());
        entity.setUserId(auditLog.userId());
        entity.setUserName(auditLog.userName());
        entity.setToolName(auditLog.toolName());
        entity.setRequestParams(auditLog.requestSummary());
        entity.setResponseSummary(auditLog.responseSummary());
        entity.setStatus(auditLog.status());
        entity.setErrorMessage(auditLog.errorMessage());
        entity.setDurationMs(auditLog.durationMs());
        auditLogMapper.insert(entity);
        log.info("audit tool={} userId={} status={} durationMs={}",
                auditLog.toolName(), auditLog.userId(), auditLog.status(), auditLog.durationMs());
    }

    public List<AuditLog> recent() {
        return auditLogMapper.findRecent()
                .stream()
                .map(entity -> new AuditLog(
                        entity.getCreateTime(),
                        entity.getUserId(),
                        entity.getUserName(),
                        entity.getToolName(),
                        entity.getStatus(),
                        entity.getDurationMs() != null ? entity.getDurationMs() : 0,
                        entity.getRequestParams(),
                        entity.getResponseSummary(),
                        entity.getErrorMessage()
                ))
                .toList();
    }

    public void recordToolCall(Long userId, String userName, String toolName, String status,
                               long durationMs, String requestSummary, String responseSummary, String errorMessage) {
        record(new AuditLog(new Date(), userId, userName, toolName, status,
                durationMs, truncate(requestSummary), truncate(responseSummary), truncate(errorMessage)));
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 500 ? value.substring(0, 500) : value;
    }
}
