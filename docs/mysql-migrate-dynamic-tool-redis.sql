-- Dynamic tools can access only explicitly bound Redis keys, commands, and fields.
-- Run this migration before mysql-migrate-feishu-tools.sql.

ALTER TABLE mcp_dynamic_tool
    ADD COLUMN linked_redis_permissions JSON NULL
        COMMENT '允许 runRedis 访问的 key、命令和字段' AFTER linked_data_source_ids;

UPDATE mcp_dynamic_tool
SET linked_redis_permissions = JSON_ARRAY()
WHERE linked_redis_permissions IS NULL;

ALTER TABLE mcp_dynamic_tool
    MODIFY linked_redis_permissions JSON NOT NULL
        COMMENT '允许 runRedis 访问的 key、命令和字段';
