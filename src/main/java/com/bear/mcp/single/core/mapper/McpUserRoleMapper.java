package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpUserRoleEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * mcp_user_role 表 Mapper。
 */
public interface McpUserRoleMapper {

    /**
     * 查询某个用户绑定的角色编码。
     */
    List<McpUserRoleEntity> findByUserId(@Param("userId") Long userId);

    /**
     * 管理端查看全部用户角色关系。
     */
    List<McpUserRoleEntity> findAll();

    /**
     * 重新分配用户角色前，先删除旧关系。
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 新增一条用户角色关系。
     */
    int insert(McpUserRoleEntity entity);
}
