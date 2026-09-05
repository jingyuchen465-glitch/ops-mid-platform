package com.ops.midplatform.core.mapper;

import com.ops.midplatform.core.entity.McpCommunityLikeStatEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** mcp_community_like 表 Mapper。 */
public interface McpCommunityLikeMapper {

    int insertIgnore(@Param("targetType") String targetType,
                     @Param("targetKey") String targetKey,
                     @Param("userId") Long userId);

    int delete(@Param("targetType") String targetType,
               @Param("targetKey") String targetKey,
               @Param("userId") Long userId);

    int exists(@Param("targetType") String targetType,
               @Param("targetKey") String targetKey,
               @Param("userId") Long userId);

    long countOne(@Param("targetType") String targetType,
                  @Param("targetKey") String targetKey);

    List<McpCommunityLikeStatEntity> countByTypes(@Param("targetTypes") List<String> targetTypes);

    List<String> findLikedKeys(@Param("targetType") String targetType,
                               @Param("targetKeys") List<String> targetKeys,
                               @Param("userId") Long userId);
}
