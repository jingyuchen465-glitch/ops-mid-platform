package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpSkillEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** mcp_skill 表 Mapper。 */
public interface McpSkillMapper {
    McpSkillEntity findById(@Param("id") Long id);

    McpSkillEntity findByCode(@Param("skillCode") String skillCode);

    McpSkillEntity findEnabledByCode(@Param("skillCode") String skillCode);

    List<McpSkillEntity> findShareStudioSkills(@Param("creatorId") Long creatorId);

    List<McpSkillEntity> findPublished();

    List<McpSkillEntity> findAll();

    String findMaxSkillCode();

    int insert(McpSkillEntity entity);

    int update(McpSkillEntity entity);

    int updatePublishStatus(@Param("id") Long id,
                            @Param("enabled") Integer enabled,
                            @Param("publishStatus") Integer publishStatus);
}
