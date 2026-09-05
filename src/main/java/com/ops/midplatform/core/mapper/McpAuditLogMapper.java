package com.ops.midplatform.core.mapper;

import com.ops.midplatform.core.entity.McpAuditLogEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
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

    /**
     * 统计指定时间范围内产生的工具调用次数。
     */
    int countBetween(@Param("startTime") Date startTime,
                     @Param("endTime") Date endTime);
}
