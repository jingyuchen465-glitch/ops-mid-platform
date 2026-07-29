-- 第 10 课预备：把“Agent 通过 MCP Tool 创建 API / Tool”的内置工具
-- 加到 demo-admin 的角色权限和课堂演示 Token 选择中。

INSERT IGNORE INTO mcp_role_tool (role_code, tool_name) VALUES
('ADMIN', 'create_request_config'),
('ADMIN', 'list_request_configs'),
('ADMIN', 'create_dynamic_tool'),
('ADMIN', 'list_dynamic_tools'),
('ADMIN', 'update_dynamic_tool_script');

INSERT IGNORE INTO mcp_user_tool_selection (token_id, tool_name, tool_type, is_enabled) VALUES
(1, 'create_request_config', 'BUILTIN', 1),
(1, 'list_request_configs', 'BUILTIN', 1),
(1, 'create_dynamic_tool', 'BUILTIN', 1),
(1, 'list_dynamic_tools', 'BUILTIN', 1),
(1, 'update_dynamic_tool_script', 'BUILTIN', 1);
