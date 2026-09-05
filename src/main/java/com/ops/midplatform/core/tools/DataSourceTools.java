package com.ops.midplatform.core.tools;

import com.ops.midplatform.core.datasource.ExternalDataSourceSqlExecutor;
import com.ops.midplatform.core.entity.McpDataSourceEntity;
import com.ops.midplatform.core.mapper.McpDataSourceMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 外部数据源只读查询 MCP Tools。 */
@Component
public class DataSourceTools {
    private final McpDataSourceMapper dataSourceMapper;
    private final ExternalDataSourceSqlExecutor sqlExecutor;

    public DataSourceTools(McpDataSourceMapper dataSourceMapper,
                           ExternalDataSourceSqlExecutor sqlExecutor) {
        this.dataSourceMapper = dataSourceMapper;
        this.sqlExecutor = sqlExecutor;
    }

    @Tool(name = "list_data_sources",
            description = "列出当前已发布的外部数据源，只返回 datasource_id、name、db_type 和 description，不包含连接串、用户名或密码。")
    public List<Map<String, Object>> listDataSources() {
        return dataSourceMapper.findPublished()
                .stream()
                .map(this::toSimpleMap)
                .toList();
    }

    @Tool(name = "query_data_source",
            description = "在指定已发布数据源上执行单条只读 SELECT 查询。sql 必须是单条 SELECT，不允许包含分号，返回行数有上限。")
    public Map<String, Object> queryDataSource(
            @ToolParam(description = "数据源主键 id，可从 list_data_sources 返回的 datasource_id 获取", required = true)
            Long datasourceId,
            @ToolParam(description = "单条 SELECT SQL，不允许包含分号", required = true)
            String sql
    ) {
        if (datasourceId == null) {
            throw new IllegalArgumentException("datasourceId 不能为空");
        }
        return sqlExecutor.query(datasourceId, sql);
    }

    private Map<String, Object> toSimpleMap(McpDataSourceEntity entity) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("datasource_id", entity.getId());
        item.put("name", entity.getName());
        item.put("db_type", entity.getDbType());
        item.put("description", entity.getDescription());
        return item;
    }
}
