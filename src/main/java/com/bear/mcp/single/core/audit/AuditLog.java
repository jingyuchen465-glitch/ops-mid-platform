package com.bear.mcp.single.core.audit;

import java.util.Date;

public record AuditLog(
        Date at,
        Long userId,
        String userName,
        String toolName,
        String status,
        long durationMs,
        String requestSummary,
        String responseSummary,
        String errorMessage
) {
}
