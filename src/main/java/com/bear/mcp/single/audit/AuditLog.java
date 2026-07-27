package com.bear.mcp.single.audit;

import java.time.LocalDateTime;

public record AuditLog(
        LocalDateTime at,
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
