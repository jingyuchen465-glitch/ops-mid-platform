package com.ops.midplatform.core.audit;

import java.util.Date;

/**
 * 工具调用审计日志的业务对象。
 *
 * <p>它不是数据库 Entity，而是运行链路记录审计时使用的 Info 对象。</p>
 */
public record AuditLog(
        /** 调用发生时间。 */
        Date at,
        /** 调用用户 ID。 */
        Long userId,
        /** 调用用户名，方便后台列表直接展示。 */
        String userName,
        /** 被调用的 MCP 工具名。 */
        String toolName,
        /** 调用状态，例如 SUCCESS、ERROR。 */
        String status,
        /** 本次调用耗时，单位毫秒。 */
        long durationMs,
        /** 请求参数摘要，避免审计日志保存过长内容。 */
        String requestSummary,
        /** 响应结果摘要，当前动态工具链路可按需补充。 */
        String responseSummary,
        /** 失败时的错误信息。 */
        String errorMessage
) {
}
