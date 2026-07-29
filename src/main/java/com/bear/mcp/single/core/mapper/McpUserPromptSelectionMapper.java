package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpUserPromptSelectionEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** mcp_user_prompt_selection 表 Mapper。 */
public interface McpUserPromptSelectionMapper {

    /** 查询某把 Token 当前启用的 Prompt 选择。 */
    List<McpUserPromptSelectionEntity> findEnabledByTokenId(@Param("tokenId") Long tokenId);

    /** 查询全部 Token Prompt 选择，供管理后台展示。 */
    List<McpUserPromptSelectionEntity> findAll();

    /** 重新保存 Token Prompt 选择前，先删除旧记录。 */
    int deleteByTokenId(@Param("tokenId") Long tokenId);

    /** 新增一条 Token Prompt 选择记录。 */
    int insert(McpUserPromptSelectionEntity entity);
}
