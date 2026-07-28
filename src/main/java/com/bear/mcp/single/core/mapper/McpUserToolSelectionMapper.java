package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpUserToolSelectionEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface McpUserToolSelectionMapper {

    List<McpUserToolSelectionEntity> findEnabledByTokenId(@Param("tokenId") Long tokenId);

    List<McpUserToolSelectionEntity> findAll();

    int deleteByTokenId(@Param("tokenId") Long tokenId);

    int insert(McpUserToolSelectionEntity entity);
}
