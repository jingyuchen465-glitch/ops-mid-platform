CREATE TABLE IF NOT EXISTS mcp_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    resource_uri VARCHAR(255) NOT NULL COMMENT 'MCP Resource URI',
    name VARCHAR(128) NOT NULL COMMENT '资源名称',
    description TEXT NULL COMMENT '资源描述',
    mime_type VARCHAR(128) NOT NULL DEFAULT 'text/markdown' COMMENT '资源 MIME 类型',
    object_key VARCHAR(512) NOT NULL COMMENT '对象存储 key',
    file_name VARCHAR(255) NULL COMMENT '原始文件名',
    file_size BIGINT NULL COMMENT '文件大小',
    creator_id BIGINT NULL COMMENT '创建者用户 ID',
    is_enabled TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用',
    publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态：0-草稿，1-已上架不公开，2-已上架公开',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_mcp_resource_uri (resource_uri),
    KEY idx_mcp_resource_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP Resource 资源表';

CREATE TABLE IF NOT EXISTS mcp_role_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    resource_uri VARCHAR(255) NOT NULL COMMENT 'MCP Resource URI',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_mcp_role_resource (role_code, resource_uri)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色 Resource 权限表';

CREATE TABLE IF NOT EXISTS mcp_user_resource_selection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    token_id BIGINT NOT NULL COMMENT 'Token ID',
    resource_uri VARCHAR(255) NOT NULL COMMENT 'MCP Resource URI',
    is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    KEY idx_mcp_resource_selection_token (token_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token Resource 选择表';
