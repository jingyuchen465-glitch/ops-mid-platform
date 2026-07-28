package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpAuditLogEntity;

import java.util.List;

/**
 * mcp_audit_log 表 Mapper。
 */
public interface McpAuditLogMapper {

    /**
     * 新增一条工具调用审计日志。
     */
    int insert(McpAuditLogEntity entity);

    /**
     * 查询最近审计日志，供管理后台展示。
     */
    List<McpAuditLogEntity> findRecent();
}
