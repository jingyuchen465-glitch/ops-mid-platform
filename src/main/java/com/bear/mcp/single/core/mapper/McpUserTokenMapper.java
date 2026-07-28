package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpUserTokenEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface McpUserTokenMapper {

    McpUserTokenEntity findActiveByTokenHash(@Param("tokenHash") String tokenHash);

    int updateLastUsedTime(@Param("id") Long id, @Param("lastUsedTime") java.util.Date lastUsedTime);

    List<McpUserTokenEntity> findAll();

    int insert(McpUserTokenEntity entity);

    int update(McpUserTokenEntity entity);

    int countActive();
}
