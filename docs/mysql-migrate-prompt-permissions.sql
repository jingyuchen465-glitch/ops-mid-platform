-- Prompt 权限治理：角色授权 + Token 选择。
-- 适用于已经执行过旧版 mysql-init.sql 的数据库。

CREATE TABLE IF NOT EXISTS mcp_role_prompt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    prompt_name VARCHAR(128) NOT NULL COMMENT 'MCP Prompt 名称',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_mcp_role_prompt (role_code, prompt_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色 Prompt 权限表';

CREATE TABLE IF NOT EXISTS mcp_user_prompt_selection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    token_id BIGINT NOT NULL COMMENT 'Token ID',
    prompt_name VARCHAR(128) NOT NULL COMMENT 'MCP Prompt 名称',
    is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    KEY idx_mcp_prompt_selection_token (token_id),
    CONSTRAINT fk_mcp_prompt_selection_token FOREIGN KEY (token_id) REFERENCES mcp_user_token (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token Prompt 选择表';
