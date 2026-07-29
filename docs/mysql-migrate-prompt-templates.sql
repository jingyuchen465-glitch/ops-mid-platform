-- 第 12 课：Prompt 模板创作空间与 MCP prompts/list、prompts/get。
-- 适用于已经执行过旧版 mysql-init.sql 的数据库。

CREATE TABLE IF NOT EXISTS mcp_prompt_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    prompt_name VARCHAR(128) NOT NULL COMMENT 'MCP Prompt 名称',
    title VARCHAR(128) NOT NULL COMMENT '展示标题',
    description TEXT NULL COMMENT 'Prompt 描述',
    arguments_schema JSON NOT NULL COMMENT 'MCP Prompt 参数定义 JSON 数组',
    template_content MEDIUMTEXT NOT NULL COMMENT 'Prompt 模板内容',
    linked_tool_names JSON NOT NULL COMMENT '建议使用的工具名',
    creator_id BIGINT NULL COMMENT '创建者用户 ID',
    is_enabled TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用',
    publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态：0-草稿，1-已上架不公开，2-已上架公开',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_mcp_prompt_template_name (prompt_name),
    KEY idx_mcp_prompt_template_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt 模板表';
