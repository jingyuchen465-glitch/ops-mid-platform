# Bear MCP Single

单体版 MCP Server 学习项目。第一版先保留核心链路：

- Spring AI MCP Server 暴露 `POST /mcp`
- Bearer Token 鉴权
- 内置 Tools 注册
- `tools/list` 按 token 选择过滤，并注入动态工具
- `tools/call` 拦截动态工具，执行 Groovy 脚本
- 动态脚本通过 `runRequest` 调用白名单请求配置
- 内存审计日志

## 启动

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
- `DynamicToolService`：动态工具注册、权限检查、审计入口
- `GroovyScriptEngine`：Groovy 脚本执行和 `runRequest` 白名单

## 后续扩展

下一步可以把内存数据替换成 MyBatis 表：

- `mcp_user_token`
- `mcp_user_tool_selection`
- `mcp_dynamic_tool`
- `mcp_request_config`
- `mcp_audit_log`
