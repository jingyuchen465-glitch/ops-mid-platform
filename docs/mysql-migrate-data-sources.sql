-- 第 11 课准备：新增外部数据源配置表。
-- 当前迁移只提供管理端 CRUD 所需字段，后续 query_data_source / runSql 会继续复用此表。

CREATE TABLE IF NOT EXISTS mcp_data_source (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(128) NOT NULL COMMENT '数据源名称',
    datasource_key VARCHAR(128) NOT NULL COMMENT '数据源唯一 key',
    db_type VARCHAR(16) NOT NULL COMMENT '数据库类型：MYSQL、TIDB',
    jdbc_url TEXT NOT NULL COMMENT 'JDBC URL',
    username VARCHAR(128) NOT NULL COMMENT '数据库用户名',
    password_encrypted TEXT NOT NULL COMMENT 'AES 加密后的数据库密码',
    extra_jdbc_props JSON NULL COMMENT '额外 JDBC 参数 JSON',
    description TEXT NULL COMMENT '数据源说明',
    publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态：0-草稿，1-已发布',
    last_operator_id BIGINT NULL COMMENT '最近操作人用户 ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_mcp_data_source_key (datasource_key),
    KEY idx_mcp_data_source_publish (publish_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部数据源配置表';
