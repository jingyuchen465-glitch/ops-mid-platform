package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpRequestConfigEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * mcp_request_config 表 Mapper。
 */
public interface McpRequestConfigMapper {

    /**
     * 根据 config_key 查询请求配置。
     * 动态工具脚本 runRequest(key, params) 最终会走到这里。
     */
    McpRequestConfigEntity findByConfigKey(@Param("configKey") String configKey);

    /**
     * 根据主键查询请求配置。
     */
    McpRequestConfigEntity findById(@Param("id") Long id);

    /**
     * 查询全部请求配置，供管理后台展示。
     */
    List<McpRequestConfigEntity> findAll();

    /**
     * 查询创作空间中的 HTTP API。
     * 当前课堂版展示当前用户创建的 API，并兼容没有创建者的演示数据。
     */
    List<McpRequestConfigEntity> findShareStudioApis(@Param("creatorId") Long creatorId);

    /**
     * 新增请求配置。
     */
    int insert(McpRequestConfigEntity entity);

    /**
     * 更新请求配置。
     */
    int update(McpRequestConfigEntity entity);

    /**
     * 统计启用中的请求配置数量。
     */
    int countEnabled();
}
