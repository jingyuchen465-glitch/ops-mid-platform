ALTER TABLE mcp_dynamic_tool
    ADD COLUMN publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态：0-草稿，1-已上架不公开，2-已上架公开'
    AFTER is_enabled;

UPDATE mcp_dynamic_tool
SET publish_status = CASE WHEN is_enabled = 1 THEN 1 ELSE 0 END;

