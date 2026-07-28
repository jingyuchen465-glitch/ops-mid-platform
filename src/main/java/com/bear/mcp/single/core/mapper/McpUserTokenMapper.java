package com.bear.mcp.single.core.mapper;

import com.bear.mcp.single.core.entity.McpUserTokenEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * mcp_user_token 表 Mapper。
 */
public interface McpUserTokenMapper {

    /**
     * 根据 Token SHA-256 摘要查询启用 Token。
     */
    McpUserTokenEntity findActiveByTokenHash(@Param("tokenHash") String tokenHash);

    /**
     * 鉴权成功后刷新最后使用时间。
     */
    int updateLastUsedTime(@Param("id") Long id, @Param("lastUsedTime") java.util.Date lastUsedTime);

    /**
     * 查询全部 Token，供管理后台展示。
     */
    List<McpUserTokenEntity> findAll();

    /**
     * 新增 Token。
     */
    int insert(McpUserTokenEntity entity);

    /**
     * 更新 Token。
     */
    int update(McpUserTokenEntity entity);

    /**
     * 统计启用 Token 数量。
     */
    int countActive();
}
