package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpUserToolSelectionEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * mcp_user_tool_selection 表 Mapper。
 */
public interface McpUserToolSelectionMapper {

    /**
     * 查询某把 Token 当前启用的工具选择。
     */
    List<McpUserToolSelectionEntity> findEnabledByTokenId(@Param("tokenId") Long tokenId);

    /**
     * 查询全部 Token 工具选择，供管理后台展示。
     */
    List<McpUserToolSelectionEntity> findAll();

    /**
     * 重新保存 Token 工具选择前，先删除旧记录。
     */
    int deleteByTokenId(@Param("tokenId") Long tokenId);

    /**
     * 新增一条 Token 工具选择记录。
     */
    int insert(McpUserToolSelectionEntity entity);
}
