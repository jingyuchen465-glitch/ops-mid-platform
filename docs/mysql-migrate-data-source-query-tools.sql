-- 第 11 课：把只读数据源 MCP Tools 加到 demo-admin 角色权限和课堂演示 Token 选择中。

INSERT IGNORE INTO mcp_role_tool (role_code, tool_name) VALUES
('ADMIN', 'list_data_sources'),
('ADMIN', 'query_data_source');

INSERT IGNORE INTO mcp_user_tool_selection (token_id, tool_name, tool_type, is_enabled) VALUES
(1, 'list_data_sources', 'BUILTIN', 1),
(1, 'query_data_source', 'BUILTIN', 1);
