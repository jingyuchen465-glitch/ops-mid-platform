-- 允许 demo-admin 通过 MCP Tool 直接创建 Bear Skill。

INSERT IGNORE INTO mcp_role_tool (role_code, tool_name) VALUES
('ADMIN', 'create_skill');

INSERT IGNORE INTO mcp_user_tool_selection (token_id, tool_name, tool_type, is_enabled) VALUES
(1, 'create_skill', 'BUILTIN', 1);
