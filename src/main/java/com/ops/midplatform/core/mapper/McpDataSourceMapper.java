package com.ops.midplatform.core.mapper;

import com.ops.midplatform.core.entity.McpDataSourceEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** mcp_data_source 表 Mapper。 */
public interface McpDataSourceMapper {
    McpDataSourceEntity findById(@Param("id") Long id);

    McpDataSourceEntity findByDatasourceKey(@Param("datasourceKey") String datasourceKey);

    List<McpDataSourceEntity> findAll();

    List<McpDataSourceEntity> findPublished();

    int insert(McpDataSourceEntity entity);

    int update(McpDataSourceEntity entity);

    int deleteById(@Param("id") Long id);
}
