-- 允许 demo-admin 通过 MCP Tool 渲染已授权 Prompt，供 Cursor 等客户端桥接 prompts/get。

INSERT IGNORE INTO mcp_role_tool (role_code, tool_name) VALUES
('ADMIN', 'render_prompt');

INSERT IGNORE INTO mcp_user_tool_selection (token_id, tool_name, tool_type, is_enabled) VALUES
(1, 'render_prompt', 'BUILTIN', 1);
