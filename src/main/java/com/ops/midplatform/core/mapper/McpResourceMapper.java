package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpResourceEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** mcp_resource 表 Mapper。 */
public interface McpResourceMapper {
    McpResourceEntity findById(@Param("id") Long id);

    McpResourceEntity findByUri(@Param("resourceUri") String resourceUri);

    McpResourceEntity findEnabledByUri(@Param("resourceUri") String resourceUri);

    List<McpResourceEntity> findShareStudioResources(@Param("creatorId") Long creatorId);

    List<McpResourceEntity> findPublished();

    List<McpResourceEntity> findAll();

    int insert(McpResourceEntity entity);

    int update(McpResourceEntity entity);

    int updatePublishStatus(@Param("id") Long id,
                            @Param("enabled") Integer enabled,
                            @Param("publishStatus") Integer publishStatus);

    int updateEnabled(@Param("id") Long id, @Param("enabled") Integer enabled);
}
