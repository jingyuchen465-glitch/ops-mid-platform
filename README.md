# Bear MCP Single

单体版 MCP Server 学习项目。当前版本保留核心链路，并把课堂演示数据落到数据库：

- Spring AI MCP Server 暴露 `POST /mcp`
- Bearer Token 鉴权
- 内置 Tools 注册
- `tools/list` 按 token 选择过滤，并注入动态工具
- `tools/call` 拦截动态工具，执行 Groovy 脚本
- 动态脚本通过 `runRequest` 调用白名单请求配置
- MyBatis 读取 Token、工具选择、动态工具和请求配置
- 审计日志写入 `mcp_audit_log`

项目只使用 MySQL。首次启动前需要创建数据库并导入初始化脚本；之后 Token、动态工具、请求配置和审计日志都会持久化保存。

## 启动

先创建数据库并导入初始化脚本：

```bash
mysql -u root -p -e "create database if not exists bear_mcp_single default charset utf8mb4"
mysql -u root -p bear_mcp_single < docs/mysql-init.sql
```

按自己的本地 MySQL 账号修改 `src/main/resources/application.yml`，再启动：

```bash
mvn spring-boot:run
```

默认端口：

```text
http://localhost:8090/mcp
```

默认测试 token：

```text
mcp_dev_token
```

`docs/mysql-init.sql` 会创建用户、角色、角色工具权限、Token、工具选择、动态工具、请求配置和审计等核心表，并写入 `mcp_dev_token`、`echo_dynamic` 等示例数据。项目启动不会自动重建表，因此后续调用的审计日志会保留。

## MCP 调用示例

Streamable HTTP 需要先 `initialize`，并把响应头里的 `Mcp-Session-Id` 带到后续请求。

```bash
curl -i -X POST http://localhost:8090/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'Authorization: Bearer mcp_dev_token' \
  -d '{"jsonrpc":"2.0","id":0,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"curl","version":"0.1"}}}'
```

查看工具列表：

```bash
curl -X POST http://localhost:8090/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'Authorization: Bearer mcp_dev_token' \
  -H 'Mcp-Session-Id: <initialize 返回的会话 ID>' \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'
```

调用动态工具：

```bash
curl -X POST http://localhost:8090/mcp \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer mcp_dev_token' \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"echo_dynamic","arguments":{"message":"hello mcp"}}}'
```

## 当前代码入口

- `SingleMcpApplication`：启动类
- `ApiKeyAuthFilter`：MCP Token 鉴权
- `McpToolsListFilter`：过滤工具列表并注入动态工具
- `McpToolsCallFilter`：拦截动态工具调用
- `TokenService`：计算 Bearer Token 的 SHA-256 哈希，并从用户、角色、权限关系中构建鉴权上下文
- `ToolSelectionService`：从 `mcp_user_tool_selection` 读取 token 选择
- `DynamicToolService`：从 `mcp_dynamic_tool` 读取动态工具，做权限检查和审计入口
- `RequestConfigService`：从 `mcp_request_config` 读取完整企业请求配置，执行 MOCK/HTTP，并应用默认参数、超时和限流
- `GroovyScriptEngine`：Groovy 脚本执行和 `runRequest` 白名单

## 当前表

- `mcp_user`：用户身份
- `mcp_role`、`mcp_user_role`：角色定义和用户角色关系
- `mcp_role_tool`：角色拥有的工具权限
- `mcp_user_token`：Token 哈希、展示前缀、权限范围、有效期和使用记录
- `mcp_user_tool_selection`：当前 token 选择加载哪些工具
- `mcp_dynamic_tool`：动态工具描述、参数 schema、Groovy 脚本和白名单
- `mcp_request_config`：`runRequest` 可调用的完整请求配置，包含协议、参数、超时、限流、发布和 SOA/Hessian 扩展字段
- `mcp_audit_log`：工具调用审计日志

数据库不保存明文 Token；鉴权时对请求中的 Bearer Token 计算 SHA-256，再查询 `mcp_user_token.token_hash`。
