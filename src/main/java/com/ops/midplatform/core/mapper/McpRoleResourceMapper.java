package com.ops.midplatform.core.mapper;

import com.ops.midplatform.core.entity.McpRoleResourceEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/** mcp_role_resource 表 Mapper。 */
public interface McpRoleResourceMapper {
    List<McpRoleResourceEntity> findByRoleCodes(@Param("roleCodes") Collection<String> roleCodes);

    List<McpRoleResourceEntity> findAll();

    int deleteByRoleCode(@Param("roleCode") String roleCode);

    int insert(McpRoleResourceEntity entity);
}
