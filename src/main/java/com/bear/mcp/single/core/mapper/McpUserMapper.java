package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpUserEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface McpUserMapper {
    McpUserEntity findEnabledById(@Param("id") Long id);

    McpUserEntity findEnabledByUsername(@Param("username") String username);

    List<McpUserEntity> findAll();

    int insert(McpUserEntity entity);

    int update(McpUserEntity entity);

    int countAll();
}
