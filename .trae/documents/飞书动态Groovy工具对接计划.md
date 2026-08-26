# 飞书 MCP 工具对接动态 Groovy 实施计划

## 一、目标摘要

把飞书 API 的对接能力以「请求配置(mcp_request_config)+ 动态 Groovy 工具(mcp_dynamic_tool)」的方式实现,与现有 6 个 Java 内置飞书工具(feishu_send_message 等)**保留并存**。核心诉求:**请求头中的 Authorization Token 必须运行时动态注入**,不能硬编码在配置里。

实现路径:Groovy 脚本运行时先调「token 请求配置」换取 tenant_access_token,再把 token 作为参数传给业务 API 配置,由请求头占位符 `Authorization: Bearer {{token}}` 动态渲染进 header。

默认范围:2 个示例工具(`feishu_groovy_send_message` 覆盖 POST+JSON body 形态、`feishu_groovy_read_document` 覆盖 GET+路径参数形态),其余 4 个工具按附录配方扩展。

---

## 二、现状分析(基于代码探索)

### 执行链路
```
MCP tools/call
  → McpToolsCallFilter(Order 21, 权限三要素校验: 角色权限 + Token 选择 + 工具启用)
  → DynamicToolService.execute(审计日志 finally 落库)
  → GroovyScriptEngine.execute(独立线程池, 30s 超时)
      Binding: params / userId / userName / toolName / runRequest / runSql
  → runRequest(key, params)
      → ScriptRunRequest 白名单校验(mcp_dynamic_tool.linked_request_keys)
      → RequestConfigService.execute
          merge(params_default, 运行时params) → {{占位符}}替换 → OkHttp → {status, body}
```

### 关键发现
1. **核心缺口**:`RequestConfigService.executeHttp` 中 URL 和 bodyTemplate 支持 `{{参数名}}` 占位符替换,但 **headers 是直接从配置写入固定值,不做占位符替换**(第 185~187 行)。不补上这个缺口,Token 无法动态传入请求头。
2. **安全约束**:`GroovySecurityCustomizer` 禁止脚本使用 `java.net.Socket`、`Runtime`、`System` 等类,脚本**不能自己发 HTTP 请求**,必须走 `runRequest`;`JsonOutput`/`JsonSlurper` 已默认导入,可直接用于 JSON 处理。
3. **凭证现状**:application.yml `bear.feishu` 已配置自建应用凭证(app-id: `cli_aa04f68595389d01`),Java 版 `FeishuClient` 用它获取并缓存 tenant_access_token。动态 Groovy 版可复用同一组凭证,存入 token 请求配置的 `params_default`(服务端数据库存储,与 application.yml 同级保密;脚本拿不到密钥,因为 merge 发生在 `RequestConfigService` 内部)。
4. **数据库现状**:`mcp_request_config` 表为空(0 条);`mcp_dynamic_tool` 仅有 `echo_dynamic`(已发布,白名单引用了不存在的 `demo_clock`)。权限先例:ADMIN 角色 + token_id=1(`mcp_dev_token`)已授权过内置飞书工具。
5. **创建/发布/授权入口**:
   - MCP 工具:`create_request_config`(创建即 is_enabled=1、publish_status=1)、`create_dynamic_tool`(创建为草稿 enabled=0)、`update_dynamic_tool_script`
   - 发布:`POST /api/share/studio/tools/{id}/publish`(置 enabled=1、publish_status=2,需 admin JWT)
   - 授权:`PUT /api/admin/roles/{roleCode}/tools`、`PUT /api/admin/tokens/{tokenId}/selections`(需 admin JWT)
   - 备选:直连 MySQL(127.0.0.1:3306/bear-mcp-single, root)执行 SQL
6. `create_dynamic_tool` 校验白名单里的 config_key 必须已存在且 publish_status ≠ 0,所以**必须先建请求配置,再建动态工具**。

---

## 三、改动方案(按执行顺序)

### 改动 1:Java 增强 —— 请求头支持 `{{占位符}}`(本计划唯一的代码修改)

**文件**:`src/main/java/com/bear/mcp/single/core/request/RequestConfigService.java`

`executeHttp` 方法中,header 设置循环改为对值做占位符替换:

```java
// 原代码(第 185~187 行):
for (Map.Entry<String, String> header : config.headers().entrySet()) {
    requestBuilder.header(header.getKey(), header.getValue());
}

// 改为:
for (Map.Entry<String, String> header : config.headers().entrySet()) {
    requestBuilder.header(header.getKey(), replace(header.getValue(), params));
}
```

原因:URL/bodyTemplate 已有同款占位符机制,headers 补齐是对称增强;`hasHeader(config.headers(), "Content-Type")` 判断基于 header 名,不受影响;`replace()` 内部用 `Matcher.quoteReplacement`,值中含 `$`、`\`、`{}` 都安全。

**顺带更新两处描述**(保持文档与行为一致):
- `RequestConfig.java` 第 25 行 headers 字段注释 → `/** HTTP 请求头,值支持 {{参数名}} 占位符。 */`
- `RequestConfigTools.java` create_request_config 的 `headers` 参数 `@ToolParam` 描述 → 补充「值支持 {{key}} 占位符,如 {"Authorization":"Bearer {{token}}"}」

### 改动 2:创建 3 个飞书请求配置(通过 MCP `create_request_config`,无需改代码)

执行方式:用 `mcp_dev_token` 通过 MCP `tools/call` 调用 `create_request_config`(实现阶段由我通过 run_mcp 执行)。

| 字段 | feishu_tenant_token | feishu_im_send_message | feishu_docx_raw_content |
|---|---|---|---|
| name | 飞书获取tenant_access_token | 飞书发送消息 | 飞书读取文档纯文本 |
| method | POST | POST | GET |
| url | `https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal` | `https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type={{receive_id_type}}` | `https://open.feishu.cn/open-apis/docx/v1/documents/{{document_id}}/raw_content` |
| headers | 不传 | `{"Authorization":"Bearer {{token}}"}` | `{"Authorization":"Bearer {{token}}"}` |
| body_template | `{"app_id":"{{app_id}}","app_secret":"{{app_secret}}"}` | `{"receive_id":"{{receive_id}}","msg_type":"{{msg_type}}","content":"{{content}}"}` | 不传(GET) |
| params_default | `{"app_id":"cli_aa04f68595389d01","app_secret":"<取 application.yml bear.feishu.app-secret 值>"}` | 不传 | 不传 |
| rate_limit_per_minute | 30 | 0 | 0 |
| category | feishu | feishu | feishu |

说明:token 配置设 30 次/分钟限流,防止脚本滥用拖垮凭证接口;飞书 token 端点对同一应用在有效期内返回相同 token,课堂版不做缓存、每次实时获取即可。

### 改动 3:创建 2 个动态 Groovy 工具(通过 MCP `create_dynamic_tool`)

**工具 A:`feishu_groovy_send_message`**

- tool_description:通过飞书发送消息(动态 Groovy 版)。text 类型直接传文本,post/interactive 传 JSON 字符串。Token 自动获取,也可通过 token 参数外部传入。
- linked_request_keys:`["feishu_tenant_token","feishu_im_send_message"]`
- input_schema:
```json
{
  "type": "object",
  "properties": {
    "receive_id": {"type": "string", "description": "接收者 id,配合 receive_id_type 使用"},
    "receive_id_type": {"type": "string", "description": "open_id / user_id / email / chat_id,默认 open_id"},
    "msg_type": {"type": "string", "description": "text / post / interactive,默认 text"},
    "content": {"type": "string", "description": "消息内容。text 直接传文本;post/interactive 传飞书要求的 JSON 字符串"},
    "token": {"type": "string", "description": "可选。外部已获取的 tenant_access_token,不传则自动获取"}
  },
  "required": ["receive_id", "content"]
}
```
- groovy_script(已按安全限制器规则自检:无被禁类/被禁方法):
```groovy
def receiveId = params.receive_id
if (receiveId == null || receiveId.toString().isBlank()) {
    return [success: false, message: "receive_id 不能为空"]
}
def receiveIdType = params.receive_id_type ?: "open_id"
def msgType = (params.msg_type ?: "text").toString().toLowerCase()
def content = params.content
if (content == null || content.toString().isBlank()) {
    return [success: false, message: "content 不能为空"]
}
def feishuContent = "text".equals(msgType) ? JsonOutput.toJson([text: content.toString()]) : content.toString()

// Token 动态注入:优先用调用方传入的 token,否则实时获取
def token = params.token
if (token == null || token.toString().isBlank()) {
    def tokenResp = runRequest.runRequest("feishu_tenant_token", [:])
    def tokenBody = new JsonSlurper().parseText(tokenResp.body.toString())
    if (tokenBody.code != null && tokenBody.code != 0) {
        return [success: false, message: "获取飞书 tenant_access_token 失败: " + tokenBody.msg]
    }
    token = tokenBody.tenant_access_token
}

def resp = runRequest.runRequest("feishu_im_send_message", [
        token          : token.toString(),
        receive_id     : receiveId.toString(),
        receive_id_type: receiveIdType.toString(),
        msg_type       : msgType,
        content        : feishuContent
])
def body = new JsonSlurper().parseText(resp.body.toString())
if (body.code != null && body.code != 0) {
    return [success: false, message: "飞书接口返回错误 code=" + body.code + ", msg=" + body.msg]
}
return [
        success        : true,
        message        : "消息发送成功(动态 Groovy 版)",
        message_id     : body.data?.message_id,
        receive_id_type: receiveIdType
]
```

**工具 B:`feishu_groovy_read_document`**

- tool_description:读取飞书新版云文档(docx)纯文本内容(动态 Groovy 版)。
- linked_request_keys:`["feishu_tenant_token","feishu_docx_raw_content"]`
- input_schema:
```json
{
  "type": "object",
  "properties": {
    "document_id": {"type": "string", "description": "文档 ID,形如 doxcn_xxx,取自文档链接"},
    "token": {"type": "string", "description": "可选。外部已获取的 tenant_access_token,不传则自动获取"}
  },
  "required": ["document_id"]
}
```
- groovy_script:
```groovy
def documentId = params.document_id
if (documentId == null || documentId.toString().isBlank()) {
    return [success: false, message: "document_id 不能为空"]
}

def token = params.token
if (token == null || token.toString().isBlank()) {
    def tokenResp = runRequest.runRequest("feishu_tenant_token", [:])
    def tokenBody = new JsonSlurper().parseText(tokenResp.body.toString())
    if (tokenBody.code != null && tokenBody.code != 0) {
        return [success: false, message: "获取飞书 tenant_access_token 失败: " + tokenBody.msg]
    }
    token = tokenBody.tenant_access_token
}

def resp = runRequest.runRequest("feishu_docx_raw_content", [
        token      : token.toString(),
        document_id: documentId.toString()
])
def body = new JsonSlurper().parseText(resp.body.toString())
if (body.code != null && body.code != 0) {
    return [success: false, message: "飞书接口返回错误 code=" + body.code + ", msg=" + body.msg]
}
return [
        success: true,
        message: "读取成功(动态 Groovy 版)",
        content: body.data?.content
]
```

### 改动 4:发布 + 授权

`create_dynamic_tool` 创建的是草稿(enabled=0),必须发布并授权后才能被 tools/call。

**优先方式:admin REST(需先 `POST /api/admin/auth/login` 获取 JWT)**
1. `POST /api/share/studio/tools/{id}/publish` → 两个工具分别发布(enabled=1, publish_status=2)
2. `PUT /api/admin/roles/ADMIN/tools` → 角色工具列表中追加两个新工具名
3. `PUT /api/admin/tokens/1/selections` → token_id=1 的工具选择中追加两个新工具(tool_type=DYNAMIC, enabled=1)

**备选方式:直连 MySQL 执行 SQL**(管理账号不可用时):
```sql
UPDATE mcp_dynamic_tool SET publish_status = 2, enabled = 1
 WHERE tool_name IN ('feishu_groovy_send_message', 'feishu_groovy_read_document');

INSERT INTO mcp_role_tool (role_code, tool_name, create_time)
VALUES ('ADMIN', 'feishu_groovy_send_message', NOW()),
       ('ADMIN', 'feishu_groovy_read_document', NOW());

INSERT INTO mcp_user_tool_selection (token_id, tool_name, tool_type, enabled)
VALUES (1, 'feishu_groovy_send_message', 'DYNAMIC', 1),
       (1, 'feishu_groovy_read_document', 'DYNAMIC', 1);
```
(执行前先 `SELECT * FROM mcp_role_tool LIMIT 3` 确认列结构与 echo_dynamic 的授权样例行,按实际列名调整;若用 REST 方式则无需 SQL。)

### 改动 5:重启应用并验证

改动 1 是 Java 代码变更,需要重新编译并重启 Spring Boot 应用(server.port=8090)后才生效;改动 2~4 是数据库配置,重启前后均可执行。

---

## 四、假设与决策

| # | 决策 | 理由 |
|---|---|---|
| 1 | Token 由脚本运行时通过 `feishu_tenant_token` 配置实时获取,同时支持可选 `params.token` 外部覆盖 | tenant_access_token 是应用级凭证、2 小时过期,不应硬编码;可选覆盖兼顾「调用方已有 token 直接复用」的场景,两者都满足"动态传入" |
| 2 | 不做 token 缓存,每次调用实时获取 | 脚本无状态无法缓存;飞书 token 端点对同一应用有效期内返回相同 token;已配 30 次/分钟限流保护 |
| 3 | app_id/app_secret 存 `params_default`(数据库),复用 application.yml 同一组凭证 | 与 application.yml 同级的服务端保密存储;merge 在 RequestConfigService 内部完成,脚本不可见密钥 |
| 4 | 新工具命名 `feishu_groovy_*` 前缀 | 工具名全局唯一,避免与内置 `feishu_*` 冲突,且便于区分两套实现 |
| 5 | Java 内置 6 个飞书工具保留并存 | 已与用户确认;便于对比学习,不影响已授权调用 |
| 6 | 默认实现 2 个示例工具(POST/GET 两种形态) | 打通并验证完整模式;其余 4 个工具按附录配方扩展,避免一次性重复劳动 |
| 7 | 请求配置通过 MCP `create_request_config` 创建,而非直接 INSERT | 走项目自己的管理链路,自动带上 request_id、creator_id 等字段;配置创建即启用 |

## 五、验证步骤

1. **重启应用**后确认启动无报错(端口 8090)。
2. **单测请求头占位符**:`list_request_configs` 确认 3 条配置存在;先调 `feishu_groovy_read_document`(只读操作,无副作用)读取已有文档 `SPntdVGDuoZBrOx0Enicq0bqnji`(AI应用开发面试文档),预期返回 success=true 和文档内容——这同时验证了「token 实时获取 + GET + header 动态注入」。
3. **发消息验证**:调用 `feishu_groovy_send_message` 向本租户用户发送 text 测试消息(注意:只能发本租户用户/群,跨租户会返回 code=230038),预期返回 message_id。
4. **权限链路验证**:未授权 Token 调用应被拦截(报"当前 Token 未获授权调用工具"),证明权限三要素仍然生效。
5. **审计验证**:管理后台审计日志中出现两个新工具的 SUCCESS 记录(参数摘要 + 响应摘要)。
6. (可选)`params.token` 传入一个外部 token,验证覆盖逻辑生效。

## 六、附录:其余 4 个工具的扩展配方

每个飞书能力 = 1 条请求配置 + 1 个动态工具,模式完全一致:

| 工具 | 配置 key | method | 飞书路径 | 备注 |
|---|---|---|---|---|
| 读消息 | feishu_im_read_message | GET | `/im/v1/messages/{{message_id}}` | header 同 Bearer {{token}} |
| 建文档 | feishu_docx_create | POST | `/docx/v1/documents` | body `{"title":"{{title}}","folder_token":"{{folder_token}}"}`(folder_token 可选,空串处理) |
| 读表格 | feishu_sheet_read | GET | `/sheets/v3/spreadsheets/{{spreadsheet_token}}/sheets/{{sheet_id}}/values/{{range}}` | 路径占位符示例 |
| 写表格 | feishu_sheet_write | PUT | `/sheets/v2/spreadsheets/{{spreadsheet_token}}/values` | body 模板含 values 二维数组 JSON |

脚本骨架复用:参数校验 → token 获取(同一段)→ runRequest(业务 key)→ 解析 code/data → 返回结构化结果。
