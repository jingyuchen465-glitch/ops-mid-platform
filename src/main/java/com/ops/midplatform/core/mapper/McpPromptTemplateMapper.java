package com.ops.midplatform.core.mapper;

import com.ops.midplatform.core.entity.McpPromptTemplateEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** mcp_prompt_template 表 Mapper。 */
public interface McpPromptTemplateMapper {
    McpPromptTemplateEntity findById(@Param("id") Long id);

    McpPromptTemplateEntity findByName(@Param("promptName") String promptName);

    McpPromptTemplateEntity findEnabledByName(@Param("promptName") String promptName);

    List<McpPromptTemplateEntity> findShareStudioPrompts(@Param("creatorId") Long creatorId);

    List<McpPromptTemplateEntity> findPublished();

    List<McpPromptTemplateEntity> findAll();

    int insert(McpPromptTemplateEntity entity);

    int update(McpPromptTemplateEntity entity);

    int updatePublishStatus(@Param("id") Long id,
                            @Param("enabled") Integer enabled,
                            @Param("publishStatus") Integer publishStatus);
}
