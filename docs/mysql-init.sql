DROP TABLE IF EXISTS mcp_audit_log;
DROP TABLE IF EXISTS mcp_user_tool_selection;
DROP TABLE IF EXISTS mcp_role_tool;
DROP TABLE IF EXISTS mcp_user_role;
DROP TABLE IF EXISTS mcp_dynamic_tool;
DROP TABLE IF EXISTS mcp_request_config;
DROP TABLE IF EXISTS mcp_user_token;
DROP TABLE IF EXISTS mcp_role;
DROP TABLE IF EXISTS mcp_user;

CREATE TABLE mcp_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户主键',
    username VARCHAR(64) NOT NULL COMMENT '登录用户名',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密后的管理端登录密码',
    display_name VARCHAR(64) NOT NULL COMMENT '展示名称',
    is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_mcp_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP 用户表';

CREATE TABLE mcp_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色主键',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) NULL COMMENT '角色描述',
    is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_mcp_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP 角色表';

CREATE TABLE mcp_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_mcp_user_role (user_id, role_code),
    CONSTRAINT fk_mcp_user_role_user FOREIGN KEY (user_id) REFERENCES mcp_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE mcp_role_tool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    tool_name VARCHAR(128) NOT NULL COMMENT 'MCP 工具名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_mcp_role_tool (role_code, tool_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色工具权限表';

CREATE TABLE mcp_user_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Token 主键',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    token_name VARCHAR(64) NOT NULL COMMENT 'Token 名称',
    token_hash CHAR(64) NOT NULL COMMENT 'Token 的 SHA-256 哈希值，用于鉴权查询',
    token_encrypted TEXT NULL COMMENT 'AES 加密后的 Token，仅管理后台需要时使用',
    token_prefix VARCHAR(32) NOT NULL COMMENT 'Token 展示前缀，避免泄露完整 Token',
    permissions JSON NULL COMMENT 'Token 权限范围',
    expire_time DATETIME NULL COMMENT '过期时间',
    last_used_time DATETIME NULL COMMENT '最后使用时间',
    last_used_ip VARCHAR(64) NULL COMMENT '最后使用 IP',
    is_active TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_mcp_user_token_hash (token_hash),
    CONSTRAINT fk_mcp_user_token_user FOREIGN KEY (user_id) REFERENCES mcp_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP Token 表';

CREATE TABLE mcp_user_tool_selection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    token_id BIGINT NOT NULL COMMENT 'Token ID',
    tool_name VARCHAR(128) NOT NULL COMMENT '工具名',
    tool_type VARCHAR(32) NOT NULL COMMENT 'BUILTIN / DYNAMIC',
    is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    KEY idx_mcp_tool_selection_token (token_id),
    CONSTRAINT fk_mcp_tool_selection_token FOREIGN KEY (token_id) REFERENCES mcp_user_token (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token 工具选择表';

CREATE TABLE mcp_dynamic_tool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tool_name VARCHAR(128) NOT NULL COMMENT 'MCP 工具名',
    tool_description TEXT NOT NULL COMMENT '工具描述',
    input_schema JSON NOT NULL COMMENT 'MCP inputSchema',
    groovy_script TEXT NOT NULL COMMENT 'Groovy 脚本',
    linked_request_keys JSON NOT NULL COMMENT '允许 runRequest 调用的配置 key',
    is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    UNIQUE KEY uk_mcp_dynamic_tool_name (tool_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态工具表';

CREATE TABLE mcp_request_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    request_id VARCHAR(32) NOT NULL COMMENT '接口唯一 ID，例如 API0000000001',
    config_key VARCHAR(128) NOT NULL COMMENT '请求配置 key',
    name VARCHAR(128) NOT NULL COMMENT '配置名称',
    type VARCHAR(16) NOT NULL COMMENT '协议类型：HTTP、SOA、HESSIAN、MOCK',
    method VARCHAR(16) NULL COMMENT 'HTTP 方法：GET、POST 等；MOCK 类型可为 MOCK',
    url TEXT NULL COMMENT '请求 URL',
    headers JSON NULL COMMENT '请求头 JSON',
    body_template TEXT NULL COMMENT '请求体模板',
    params_default JSON NULL COMMENT '调试及运行时默认参数 JSON',
    connect_timeout_ms INT NOT NULL DEFAULT 5000 COMMENT '连接超时毫秒',
    read_timeout_ms INT NOT NULL DEFAULT 15000 COMMENT '读取超时毫秒',
    service_name VARCHAR(255) NULL COMMENT 'SOA/Hessian 服务名称',
    method_name VARCHAR(255) NULL COMMENT 'SOA/Hessian 方法名称',
    args_schema JSON NULL COMMENT 'SOA/Hessian 参数 schema JSON',
    creator_id BIGINT NULL COMMENT '创建者用户 ID',
    is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    rate_limit_per_minute INT NOT NULL DEFAULT 0 COMMENT '每分钟限流次数，0 表示不限制',
    publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态：0-草稿，1-已上架不公开，2-已上架公开',
    description TEXT NULL COMMENT '接口描述',
    category VARCHAR(64) NULL COMMENT '接口分类',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_mcp_request_config_request_id (request_id),
    UNIQUE KEY uk_mcp_request_config_key (config_key),
    KEY idx_mcp_request_config_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请求配置表';

CREATE TABLE mcp_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    user_id BIGINT NULL COMMENT '用户 ID',
    user_name VARCHAR(64) NULL COMMENT '用户名',
    tool_name VARCHAR(128) NULL COMMENT '工具名',
    request_params TEXT NULL COMMENT '请求参数摘要',
    response_summary TEXT NULL COMMENT '响应摘要',
    status VARCHAR(32) NULL COMMENT '状态',
    error_message TEXT NULL COMMENT '错误信息',
    duration_ms BIGINT NULL COMMENT '耗时毫秒',
    KEY idx_mcp_audit_log_tool (tool_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP 审计日志表';

-- demo-admin 的课堂密码为 admin123，生产环境必须由后台重置为个人密码。
INSERT INTO mcp_user (id, username, password_hash, display_name, is_enabled) VALUES
(10001, 'demo-admin', '$2a$10$0BkEfhsDbdilg8opNJg.4ehtPFmuAp.0wEk.5qms2hcxFToCblbz2', '课堂演示管理员', 1);

INSERT INTO mcp_role (role_code, role_name, description, is_enabled) VALUES
('ADMIN', '系统管理员', '拥有全部课堂工具权限', 1),
('DEVELOPER', '开发者', '可使用开发调试工具', 1);

INSERT INTO mcp_user_role (user_id, role_code) VALUES
(10001, 'ADMIN'),
(10001, 'DEVELOPER');

INSERT INTO mcp_role_tool (role_code, tool_name) VALUES
('ADMIN', 'hello'),
('ADMIN', 'current_time'),
('ADMIN', 'system_info'),
('ADMIN', 'calculate'),
('ADMIN', 'echo_dynamic');

-- 原始 Token mcp_dev_token 只用于课堂 curl 演示，数据库只保存它的 SHA-256 哈希。
INSERT INTO mcp_user_token (
    id, user_id, token_name, token_hash, token_encrypted, token_prefix, permissions, is_active
) VALUES (
    1,
    10001,
    '课堂演示 Token',
    'bdc518f0d828a858e07375e383cbed57548ae68ad7ebb3b07df80ba380b76b43',
    NULL,
    'mcp_dev_token...',
    JSON_ARRAY('mcp:tools:read', 'mcp:tools:call'),
    1
);

INSERT INTO mcp_user_tool_selection (token_id, tool_name, tool_type, is_enabled) VALUES
(1, 'hello', 'BUILTIN', 1),
(1, 'current_time', 'BUILTIN', 1),
(1, 'system_info', 'BUILTIN', 1),
(1, 'calculate', 'BUILTIN', 1),
(1, 'echo_dynamic', 'DYNAMIC', 1);

INSERT INTO mcp_request_config (
    request_id, config_key, name, type, method, url, headers, body_template, params_default,
    connect_timeout_ms, read_timeout_ms, creator_id, is_enabled, rate_limit_per_minute,
    publish_status, description, category
) VALUES (
    'API0000000001',
    'demo_clock',
    '课堂演示时钟',
    'MOCK',
    'MOCK',
    '',
    JSON_OBJECT(),
    '',
    JSON_OBJECT('message', '默认消息'),
    5000,
    15000,
    10001,
    1,
    60,
    2,
    '用于演示动态工具通过 runRequest 调用请求配置',
    '课堂演示'
);

INSERT INTO mcp_dynamic_tool (
    tool_name, tool_description, input_schema, groovy_script, linked_request_keys, is_enabled
) VALUES (
    'echo_dynamic',
    '动态工具示例：回显输入参数，并演示通过 runRequest 调用白名单请求配置。',
    JSON_OBJECT(
      'type', 'object',
      'properties', JSON_OBJECT('message', JSON_OBJECT('type', 'string', 'description', '要回显的内容')),
      'required', JSON_ARRAY('message')
    ),
    'def clock = runRequest.runRequest("demo_clock", [message: params.message])
return [
  message: params.message,
  currentUser: userName,
  clock: clock
]',
    JSON_ARRAY('demo_clock'),
    1
);
