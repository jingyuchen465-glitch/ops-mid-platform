CREATE TABLE IF NOT EXISTS mcp_skill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    skill_code VARCHAR(32) NOT NULL COMMENT 'Skill 编码，如 SKILL0000000001',
    name VARCHAR(128) NOT NULL COMMENT 'Skill 名称',
    description TEXT NULL COMMENT 'Skill 描述，用于 Agent 判断何时使用',
    category VARCHAR(64) NULL COMMENT '分类',
    object_key VARCHAR(512) NOT NULL COMMENT 'TOS 中的 SKILL.md 对象 key',
    file_name VARCHAR(255) NULL COMMENT '原始文件名',
    file_size BIGINT NULL COMMENT '文件大小',
    creator_id BIGINT NULL COMMENT '创建者用户 ID',
    is_enabled TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用',
    publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态：0-草稿，1-已上架不公开，2-已上架公开',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_mcp_skill_code (skill_code),
    KEY idx_mcp_skill_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP Skill 表';

INSERT IGNORE INTO mcp_role_tool (role_code, tool_name)
VALUES ('ADMIN', 'get_skill');

INSERT INTO mcp_user_tool_selection (token_id, tool_name, tool_type, is_enabled)
SELECT id, 'get_skill', 'BUILTIN', 1
FROM mcp_user_token
WHERE NOT EXISTS (
    SELECT 1
    FROM mcp_user_tool_selection
    WHERE mcp_user_tool_selection.token_id = mcp_user_token.id
      AND mcp_user_tool_selection.tool_name = 'get_skill'
);
