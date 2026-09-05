package com.ops.midplatform.core.mapper;

import com.ops.midplatform.core.entity.McpUserEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * mcp_user 表 Mapper。
 */
public interface McpUserMapper {

    /**
     * 根据用户 ID 查询启用用户。
     * MCP Token 鉴权时使用。
     */
    McpUserEntity findEnabledById(@Param("id") Long id);

    /**
     * 根据用户名查询启用用户。
     * 管理后台登录时使用。
     */
    McpUserEntity findEnabledByUsername(@Param("username") String username);

    /**
     * 查询全部用户，供管理后台展示。
     */
    List<McpUserEntity> findAll();

    /**
     * 新增用户。
     */
    int insert(McpUserEntity entity);

    /**
     * 更新用户。
     */
    int update(McpUserEntity entity);

    /**
     * 统计用户数量。
     */
    int countAll();
}
