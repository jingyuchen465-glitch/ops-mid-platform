package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpRolePromptEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/** mcp_role_prompt 表 Mapper。 */
public interface McpRolePromptMapper {

    /** 根据角色编码查询角色 Prompt 权限。 */
    List<McpRolePromptEntity> findByRoleCodes(@Param("roleCodes") Collection<String> roleCodes);

    /** 查询全部角色 Prompt 权限，供管理后台展示。 */
    List<McpRolePromptEntity> findAll();

    /** 重新保存角色 Prompt 权限前，先删除旧记录。 */
    int deleteByRoleCode(@Param("roleCode") String roleCode);

    /** 新增一条角色 Prompt 权限。 */
    int insert(McpRolePromptEntity entity);
}
