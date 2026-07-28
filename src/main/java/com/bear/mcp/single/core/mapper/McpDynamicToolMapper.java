package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpDynamicToolEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * mcp_dynamic_tool 表 Mapper。
 */
public interface McpDynamicToolMapper {

    /**
     * 根据工具名查询启用中的动态工具。
     */
    McpDynamicToolEntity findEnabledByName(@Param("toolName") String toolName);

    /**
     * 查询全部动态工具，供管理后台展示。
     */
    List<McpDynamicToolEntity> findAll();

    /**
     * 新增动态工具配置。
     */
    int insert(McpDynamicToolEntity entity);

    /**
     * 更新动态工具配置。
     */
    int update(McpDynamicToolEntity entity);

    /**
     * 统计启用中的动态工具数量。
     */
    int countEnabled();
}
