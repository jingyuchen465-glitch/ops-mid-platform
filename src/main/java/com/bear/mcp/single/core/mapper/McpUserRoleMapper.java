package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpUserRoleEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface McpUserRoleMapper {
    List<McpUserRoleEntity> findByUserId(@Param("userId") Long userId);

    int deleteByUserId(@Param("userId") Long userId);

    int insert(McpUserRoleEntity entity);
}
