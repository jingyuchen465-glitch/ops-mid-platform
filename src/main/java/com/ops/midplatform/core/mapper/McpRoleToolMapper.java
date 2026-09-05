package com.ops.midplatform.core.mapper;

import com.ops.midplatform.core.entity.McpRoleToolEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * mcp_role_tool 表 Mapper。
 */
public interface McpRoleToolMapper {

    /**
     * 根据角色编码查询角色工具权限。
     * 这是用户通过角色获得的工具资格上限。
     */
    List<McpRoleToolEntity> findByRoleCodes(@Param("roleCodes") Collection<String> roleCodes);

    /**
     * 查询全部角色工具权限，供管理后台展示。
     */
    List<McpRoleToolEntity> findAll();

    /**
     * 保存某个角色的新工具权限前，先删除旧记录。
     */
    int deleteByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 新增一条角色工具权限。
     */
    int insert(McpRoleToolEntity entity);
}
