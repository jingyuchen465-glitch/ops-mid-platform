package com.bear.mcp.single.core.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 外部数据源只读 SQL 执行器。 */
@Component
public class ExternalDataSourceSqlExecutor {
    private static final int MAX_SQL_LENGTH = 10000;
    private static final int MAX_QUERY_ROWS = 500;

    private final ExternalDataSourcePoolManager poolManager;

    public ExternalDataSourceSqlExecutor(ExternalDataSourcePoolManager poolManager) {
        this.poolManager = poolManager;
    }

    public Map<String, Object> query(long datasourceId, String sql) {
        validateSelectSql(sql);
        HikariDataSource dataSource = poolManager.getPool(datasourceId);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<Map<String, Object>> rows = new ArrayList<>();
            int count = 0;
            boolean truncated = false;
            while (resultSet.next()) {
                if (count >= MAX_QUERY_ROWS) {
                    truncated = true;
                    break;
                }
                LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                }
                rows.add(row);
                count++;
            }
            return Map.of(
                    "datasource_id", datasourceId,
                    "row_count", rows.size(),
                    "max_rows", MAX_QUERY_ROWS,
                    "truncated", truncated,
                    "rows", rows
            );
        } catch (Exception e) {
            throw new RuntimeException("查询执行失败: " + e.getMessage(), e);
        }
    }

    private void validateSelectSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL 不能为空");
        }
        String trimmed = sql.trim();
        if (trimmed.length() > MAX_SQL_LENGTH) {
            throw new IllegalArgumentException("SQL 过长，最大 " + MAX_SQL_LENGTH + " 字符");
        }
        if (trimmed.contains(";")) {
            throw new IllegalArgumentException("只允许单条 SELECT SQL，不允许包含分号");
        }
        if (!trimmed.regionMatches(true, 0, "SELECT", 0, 6)) {
            throw new IllegalArgumentException("只允许执行 SELECT 查询");
        }
    }
}
