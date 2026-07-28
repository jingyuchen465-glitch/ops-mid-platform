package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpRoleEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface McpRoleMapper {
    List<McpRoleEntity> findEnabledByRoleCodes(@Param("roleCodes") Collection<String> roleCodes);

    List<McpRoleEntity> findAll();

    int insert(McpRoleEntity entity);

    int update(McpRoleEntity entity);

    int countAll();
}
