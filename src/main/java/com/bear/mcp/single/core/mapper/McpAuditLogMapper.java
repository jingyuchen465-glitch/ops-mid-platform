package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpAuditLogEntity;

import java.util.List;

public interface McpAuditLogMapper {

    int insert(McpAuditLogEntity entity);

    List<McpAuditLogEntity> findRecent();
}
