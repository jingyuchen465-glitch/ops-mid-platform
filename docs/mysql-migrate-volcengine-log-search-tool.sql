-- Day 21 / 第三章：内置日志查询 Tool（search_yanque_admin_logs）权限与课堂演示 Token 选择。
-- Tool 直接查询火山引擎日志服务（TLS）的 yanque-admin 日志主题，属于 BUILTIN 工具。

INSERT IGNORE INTO mcp_role_tool (role_code, tool_name) VALUES
('ADMIN', 'search_yanque_admin_logs');

INSERT IGNORE INTO mcp_user_tool_selection (token_id, tool_name, tool_type, is_enabled) VALUES
(1, 'search_yanque_admin_logs', 'BUILTIN', 1);