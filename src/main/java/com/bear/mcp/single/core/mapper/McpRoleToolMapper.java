package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpRoleToolEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface McpRoleToolMapper {
    List<McpRoleToolEntity> findByRoleCodes(@Param("roleCodes") Collection<String> roleCodes);

    List<McpRoleToolEntity> findAll();

    int deleteByRoleCode(@Param("roleCode") String roleCode);

    int insert(McpRoleToolEntity entity);
}
