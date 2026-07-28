package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpDynamicToolEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface McpDynamicToolMapper {

    McpDynamicToolEntity findEnabledByName(@Param("toolName") String toolName);

    List<McpDynamicToolEntity> findAll();

    int insert(McpDynamicToolEntity entity);

    int update(McpDynamicToolEntity entity);

    int countEnabled();
}
