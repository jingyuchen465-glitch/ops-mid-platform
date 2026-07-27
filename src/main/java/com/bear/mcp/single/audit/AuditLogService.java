package com.bear.mcp.single.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final List<AuditLog> logs = new ArrayList<>();

    public synchronized void record(AuditLog auditLog) {
        logs.add(auditLog);
        log.info("audit tool={} userId={} status={} durationMs={}",
                auditLog.toolName(), auditLog.userId(), auditLog.status(), auditLog.durationMs());
    }

    public synchronized List<AuditLog> recent() {
        int from = Math.max(0, logs.size() - 100);
        return List.copyOf(logs.subList(from, logs.size()));
    }

    public void recordToolCall(Long userId, String userName, String toolName, String status,
                               long durationMs, String requestSummary, String responseSummary, String errorMessage) {
        record(new AuditLog(LocalDateTime.now(), userId, userName, toolName, status,
                durationMs, truncate(requestSummary), truncate(responseSummary), truncate(errorMessage)));
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 500 ? value.substring(0, 500) : value;
    }
}
