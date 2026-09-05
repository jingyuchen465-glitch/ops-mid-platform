package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpRoleEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * mcp_role 表 Mapper。
 */
public interface McpRoleMapper {

    /**
     * 根据角色编码查询启用角色。
     * Token 鉴权时用它过滤掉已禁用角色。
     */
    List<McpRoleEntity> findEnabledByRoleCodes(@Param("roleCodes") Collection<String> roleCodes);

    /**
     * 查询全部角色，供管理后台展示。
     */
    List<McpRoleEntity> findAll();

    /**
     * 新增角色。
     */
    int insert(McpRoleEntity entity);

    /**
     * 更新角色。
     */
    int update(McpRoleEntity entity);

    /**
     * 统计角色数量。
     */
    int countAll();
}
