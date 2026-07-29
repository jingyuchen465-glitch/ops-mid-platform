-- 第 10 课后续补充：动态 Tool 绑定数据源白名单，供 Groovy 脚本 runSql 使用。
-- 适用于已经执行过旧版 mysql-init.sql 的数据库。

ALTER TABLE mcp_dynamic_tool
    ADD COLUMN linked_data_source_ids JSON NULL COMMENT '允许 runSql 查询的数据源 id' AFTER linked_request_keys;

UPDATE mcp_dynamic_tool
SET linked_data_source_ids = JSON_ARRAY()
WHERE linked_data_source_ids IS NULL;

ALTER TABLE mcp_dynamic_tool
    MODIFY linked_data_source_ids JSON NOT NULL COMMENT '允许 runSql 查询的数据源 id';
