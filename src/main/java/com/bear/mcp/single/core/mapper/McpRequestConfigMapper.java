package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpRequestConfigEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface McpRequestConfigMapper {

    McpRequestConfigEntity findByConfigKey(@Param("configKey") String configKey);

    List<McpRequestConfigEntity> findAll();

    int insert(McpRequestConfigEntity entity);

    int update(McpRequestConfigEntity entity);

    int countEnabled();
}
