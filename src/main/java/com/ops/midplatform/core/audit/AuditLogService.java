package com.ops.midplatform.core.audit;

import com.ops.midplatform.core.entity.McpAuditLogEntity;
import com.ops.midplatform.core.mapper.McpAuditLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private static final int MAX_TOOL_NAME_LENGTH = 128;

    /**
     * mcp_audit_log：工具调用审计日志表。
     */
    private final McpAuditLogMapper auditLogMapper;

    public AuditLogService(McpAuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public void record(AuditLog auditLog) {
        /*
         * AuditLog 是运行时业务对象，McpAuditLogEntity 是数据库表对象。
         * 这里统一完成转换，避免调用方关心数据库字段名。
         */
        McpAuditLogEntity entity = new McpAuditLogEntity();
        entity.setCreateTime(auditLog.at());
        entity.setUserId(auditLog.userId());
        entity.setUserName(auditLog.userName());
        entity.setToolName(truncateToolName(auditLog.toolName()));
        entity.setRequestParams(auditLog.requestSummary());
        entity.setResponseSummary(auditLog.responseSummary());
        entity.setStatus(auditLog.status());
        entity.setErrorMessage(auditLog.errorMessage());
        entity.setDurationMs(auditLog.durationMs());
        auditLogMapper.insert(entity);
        log.info("audit tool={} userId={} status={} durationMs={}",
                auditLog.toolName(), auditLog.userId(), auditLog.status(), auditLog.durationMs());
    }

    /**
     * 查询最近审计日志，供管理后台首页和审计页面展示。
     */
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

    /**
     * 记录一次工具调用。
     *
     * <p>参数和响应都先截断再落库，避免大对象或敏感长文本把审计表撑爆。</p>
     */
    public void recordToolCall(Long userId, String userName, String toolName, String status,
                               long durationMs, String requestSummary, String responseSummary, String errorMessage) {
        record(new AuditLog(new Date(), userId, userName, toolName, status,
                durationMs, truncate(requestSummary), truncate(responseSummary), truncate(errorMessage)));
    }

    /**
     * 课堂版只保留前 500 个字符，生产项目可以改成更细的脱敏和摘要策略。
     */
    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 500 ? value.substring(0, 500) : value;
    }

    private String truncateToolName(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > MAX_TOOL_NAME_LENGTH ? value.substring(0, MAX_TOOL_NAME_LENGTH) : value;
    }
}
