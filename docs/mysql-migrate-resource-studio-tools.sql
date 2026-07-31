-- 允许 demo-admin 通过 MCP Tool 直接创建 Markdown Resource。

INSERT IGNORE INTO mcp_role_tool (role_code, tool_name) VALUES
('ADMIN', 'create_resource');

INSERT IGNORE INTO mcp_user_tool_selection (token_id, tool_name, tool_type, is_enabled) VALUES
(1, 'create_resource', 'BUILTIN', 1);
