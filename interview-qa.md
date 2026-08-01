# Bear MCP Server 面试 Q&A

## 项目价值类

### Q1: 这个项目解决了什么问题？

这个项目解决的是企业内部能力如何安全、标准化地接入 AI Agent 的问题。

公司内部通常已经有很多业务 API、数据库、文档、Prompt 和脚本能力，但这些能力原本主要是给系统之间调用的，不是给 AI Agent 直接调用的。如果每个 Agent 都单独对接这些接口，会遇到接口分散、鉴权方式不统一、参数格式不统一、权限不好控制、调用过程缺少审计、新增能力需要改 Agent 代码等问题。

所以我做了一个 MCP 服务平台，把内部 API、数据源、文档、Prompt 统一封装成 MCP Tools、Prompts、Resources，让 Cursor、Codex、Claude 或自研 Agent 都能通过标准 MCP 协议接入。平台统一负责鉴权、权限、工具选择、审计和动态扩展。

一句话总结：

> 它解决的是“企业内部能力如何安全、标准化地暴露给 AI Agent 调用”的问题。

### Q2: 为什么需要 MCP，而不是直接让 Agent 调 HTTP API？

直接让 Agent 调 HTTP API 当然可以，但问题是每个 Agent 都要知道接口地址、鉴权方式、参数结构、错误处理和业务含义，这会让 Agent 和内部系统强耦合。

MCP 的价值是把这些 HTTP API 包装成标准工具。Agent 不需要关心底层是 HTTP、SOA、Hessian 还是数据库，只需要通过 `tools/list` 看到有哪些工具、每个工具的描述和参数 schema，再通过 `tools/call` 调用。

这样底层 API 发生变化时，只需要改 MCP 平台里的配置或脚本，不一定要改 Agent。同时权限、审计、限流也可以统一放在 MCP 层处理。

举例来说，订单查询接口原来可能是 `/api/sales/orders/{id}`，Agent 需要知道 URL、Header、参数和返回结构。封装成 MCP Tool 后，Agent 只看到 `query_sales_order_by_id`，知道传 `orderId` 就可以了。

一句话总结：

> 直接调 HTTP 是点对点集成，MCP 是标准化工具层。MCP 能降低 Agent 和业务系统的耦合，并统一治理权限、审计和工具描述。

### Q3: 和普通 API 网关有什么区别？

普通 API 网关主要面向系统调用，重点是路由、鉴权、限流、负载均衡、熔断等能力。它解决的是服务之间怎么访问 API。

MCP 平台面向的是 AI Agent 调用。除了基础网关能力，它还需要让 AI 理解这个工具是干什么的、什么时候该用、需要什么参数、返回什么结果。所以 MCP Tool 会有 `name`、`description`、`inputSchema`，Agent 可以通过 `tools/list` 动态发现工具。

另外，这个平台还支持动态工具编排。比如一个 MCP Tool 背后可以调用多个内部 API，或者先查数据库再调用接口。这不是普通 API 网关的主要职责。

对比：

```text
API 网关：系统 -> API
MCP 平台：AI Agent -> Tool -> API / DB / 文档 / Prompt
```

一句话总结：

> API 网关解决系统访问 API，MCP 平台解决 AI 如何发现、理解、选择并安全调用企业能力。

### Q4: 和自己写插件、Function Calling 有什么区别？

自己写插件或 Function Calling 更适合单个模型、单个应用里的工具调用，但它通常和某个客户端或某个模型框架绑定比较深。

MCP 更像一个开放标准。工具只要按照 MCP 暴露出来，Cursor、Codex、Claude、自研 Agent 都可以接，不需要每个客户端单独写一套插件。

另外，Function Calling 通常是在应用代码里注册函数，新增工具往往要改代码、发版。而这个平台支持后台配置 API 和动态工具，发布后就能出现在 `tools/list` 里，实现不发版扩展能力。

需要注意的是，Function Calling 和 MCP 不是完全对立关系。Function Calling 更偏模型或应用内部如何调用函数，MCP 更偏工具能力如何被标准化暴露、发现和治理。

一句话总结：

> Function Calling 更偏应用内部能力注册，MCP 更偏跨客户端、跨 Agent 的标准工具协议。这个平台是在 MCP 上做企业级工具治理和动态发布。

### Q5: 这个项目真实落地场景是什么？

真实场景是企业内部 AI 助手或研发提效工具。

比如研发在 Cursor、Codex 或自研 Agent 里问“查订单 id 为 16 的订单”，AI 客户端通过 MCP 发现订单查询工具，调用后返回业务系统里的订单详情。

再比如排查问题时，可以封装日志查询、订单查询、支付查询、退款查询、飞书文档读取等工具。Agent 可以根据用户问题自动组合这些工具，完成问题定位。

对业务侧，也可以把常用数据查询封装成受控工具，比如查询校区订单金额、支付成功流水、学员信息等。这样 AI 能帮人查数据，但所有能力都经过权限、Token、审计和数据源控制，不是直接裸连数据库。

一句话总结：

> 它适合做企业内部 AI Agent 的工具中心，比如研发排障、订单查询、退款查询、日志检索、数据分析、飞书文档读取等场景。

## 连贯回答模板

如果面试官连续问项目价值，可以整合成下面这段：

> 这个项目主要解决企业内部能力接入 AI Agent 的问题。公司内部有很多已有 API、数据库、文档和 Prompt，但它们原本不是给 AI Agent 直接调用的。如果让每个 Agent 直接调 HTTP API，会造成接口分散、鉴权不统一、权限不好控、审计缺失，而且 Agent 和业务系统耦合很重。
>
> 所以我基于 MCP 做了一层标准化工具平台，把 HTTP、SOA、Hessian、数据源、飞书文档等能力封装成 MCP Tools、Prompts、Resources。Agent 通过 `tools/list` 动态发现工具，通过 `tools/call` 调用工具，不需要关心底层接口细节。
>
> 它和普通 API 网关不同，API 网关主要面向系统调用，解决路由、鉴权、限流；MCP 平台面向 AI Agent，需要提供工具描述、参数 schema、动态发现、工具选择、权限隔离和审计。它和 Function Calling 也不冲突，Function Calling 更偏单应用内注册函数，MCP 更偏跨客户端的标准工具协议。
>
> 真实落地场景包括研发排障、订单查询、退款查询、日志检索、数据分析、飞书文档读取等。比如用户在 Cursor 或自研 Agent 里说“查询订单 id 为 16”，Agent 会从 MCP 工具列表里选择订单查询工具，传入 `orderId=16`，服务端再去调用真实业务 API 并返回结果。

## 架构设计类

### Q1: 整体架构怎么拆的？

这个项目是一个多模块 Spring Boot 项目，整体按“启动入口、协议网关、核心业务、工具能力、管理后台、扩展能力”来拆。

主要模块包括：

```text
mcp-server-bootstrap   启动入口，聚合其他模块配置
mcp-server-gateway     MCP 请求网关，负责鉴权、请求拦截、tools/list 和 tools/call 处理
mcp-server-core        核心业务，包括用户、Token、权限、动态工具、API 配置、审计、数据源
mcp-server-tools       内置 MCP Tool 注册和实现
mcp-server-admin       管理后台和社区页面，管理工具、Prompt、Token、API、权限等
mcp-server-prompt      MCP Prompts 能力扩展
mcp-server-resource    MCP Resources 能力扩展
mcp-server-storage     对象存储封装，比如火山 TOS
```

整体调用链路可以概括为：

```text
AI Client / 自研 Agent
        ↓ /mcp + Bearer Token
gateway 鉴权和协议拦截
        ↓
core 查询用户、Token、权限、工具配置
        ↓
tools / dynamic tool 执行业务能力
        ↓
内部 API / 数据库 / 文档 / Prompt
```

一句话总结：

> 架构上是以 MCP 协议网关为入口，core 做能力治理和业务模型，tools/admin/prompt/resource/storage 分别承载具体能力和管理入口。

### Q2: 为什么拆成 core、gateway、tools、admin？

拆模块主要是为了让职责边界清楚，避免所有逻辑堆在一个 Spring Boot 工程里。

`core` 是核心业务层，里面放实体、Mapper、Service，以及用户、权限、Token、动态工具、API 配置、审计日志、数据源等公共能力。它不关心页面，也不直接关心 MCP 协议细节。

`gateway` 是协议入口层，主要处理 MCP 请求相关的事情，比如 API Key 鉴权、用户上下文构建、`tools/list` 响应改写、`tools/call` 动态工具拦截。它更偏请求链路和协议适配。

`tools` 是内置工具层，里面用 Spring AI 的 `@Tool` 暴露一些固定工具，比如系统信息、时间、计算器、API 配置管理、Prompt 配置管理、数据源查询等。

`admin` 是管理后台层，提供 Thymeleaf 页面和 REST API，用来管理用户、角色、Token、Tools、Prompts、Resources、API 配置、审批等。

一句话总结：

> `core` 管业务模型，`gateway` 管 MCP 请求链路，`tools` 管内置工具能力，`admin` 管后台配置和运营入口。

### Q3: MCP 请求链路是怎样的？

MCP 客户端会请求 `/mcp`，请求头里带 Bearer Token，例如：

```text
Authorization: Bearer mcp_xxx
```

服务端首先经过 `ApiKeyAuthFilter`，从 Header 或查询参数中提取 token，校验 token 是否有效。校验通过后，会查出用户 ID、角色、工具权限、tokenId、客户端 IP 等信息，并放入 `McpUserContext`。

之后 MCP 请求会进入不同的处理链路：

```text
initialize     初始化 MCP 会话和能力声明
tools/list     返回当前 token 可用工具
tools/call     调用某个工具
prompts/list   返回当前 token 可用 Prompt
resources/list 返回当前 token 可用 Resource
```

对于普通 MCP 能力，Spring AI MCP Server 会按标准协议处理。项目里又额外通过 Filter 对 `tools/list`、`tools/call`、Prompts、Resources 做了增强，以支持权限过滤和动态工具。

一句话总结：

> 请求进来先鉴权并构建用户上下文，再根据 MCP method 分流处理，核心是保证每次 `tools/list` 和 `tools/call` 都是在当前用户和当前 token 权限下执行。

### Q4: tools/list 是怎么处理的？

`tools/list` 的作用是让 MCP 客户端知道当前有哪些工具可以用。

项目里不是简单返回所有工具，而是在 `McpToolsListFilter` 中拦截 `POST /mcp` 的 `tools/list` 请求。它会先让 Spring AI MCP Server 生成原始工具列表，然后再根据当前用户和 token 做二次过滤与增强。

具体逻辑是：

```text
1. 从 McpUserContext 中拿到 userId 和 tokenId
2. 查询这个 token 选择了哪些工具
3. 查询用户角色允许访问哪些工具
4. 保留同时满足“token 已选择”和“角色有权限”的内置工具
5. 从动态工具库中加载用户可用的动态工具
6. 合并内置工具和动态工具，按 MCP Tool 格式返回
```

这样不同用户、不同 token 调 `tools/list`，看到的工具列表可以不一样。

一句话总结：

> `tools/list` 是工具发现入口，项目在这里做了 token 级工具选择、角色权限过滤和动态工具注入。

### Q5: tools/call 是怎么处理的？

`tools/call` 的作用是调用某一个 MCP Tool。

项目里分两种情况：

```text
内置工具：交给 Spring AI 的 @Tool 方法执行
动态工具：由 McpToolsCallFilter 拦截后走 DynamicToolService 执行
```

当请求进来时，`McpToolsCallFilter` 会解析请求体里的工具名。如果这个工具名不是动态工具，就继续往后走，让 Spring AI 调用对应的内置 `@Tool` 方法。

如果是动态工具，就不会走普通 `@Tool` 方法，而是：

```text
1. 根据 toolName 查询动态工具库
2. 校验工具是否存在、是否启用、用户是否有权限
3. 读取工具的 Groovy 脚本、参数 schema、关联 API ID、关联数据源 ID
4. 构建脚本上下文 params、userId、userName、runRequest、runSql 等
5. 执行 Groovy 脚本
6. 把结果包装成 MCP JSON-RPC 响应
7. 异步记录审计日志
```

一句话总结：

> `tools/call` 对内置工具走静态方法调用，对动态工具走数据库配置加 Groovy 脚本执行，从而实现不发版新增工具。

### Q6: 动态工具和内置工具有什么区别？

内置工具是写在 Java 代码里的，通常用 Spring AI 的 `@Tool` 注解注册。比如系统信息、当前时间、计算器、API 配置管理、Prompt 配置管理等。这类工具稳定、基础、通用，但新增或修改通常需要改代码、重新发布。

动态工具是用户在后台配置出来的，工具名称、描述、参数 schema、Groovy 脚本、关联 API 列表都存在数据库里。发布后，`tools/list` 会把它动态转换成 MCP Tool；`tools/call` 时再从数据库加载脚本执行。

对比：

```text
内置工具：代码中固定注册，适合基础能力
动态工具：数据库配置生成，适合业务 API 封装和轻量编排
```

动态工具的优势是不需要发版就能扩展能力。比如把订单查询 API、退款查询 API、日志查询 API 配成工具后，AI 客户端马上可以通过 MCP 调用。

一句话总结：

> 内置工具偏平台基础能力，动态工具偏业务扩展能力；内置工具靠代码发布，动态工具靠后台配置发布。

### Q7: Prompt、Resource、Tool 在 MCP 里分别是什么？

在 MCP 里，这三个概念承担的角色不一样。

`Tool` 是可执行能力。它通常有输入参数，调用后会产生结果。比如查询订单、查询天气、执行 SQL、读取飞书文档等。用户问一个具体任务时，Agent 会选择合适的 Tool 来完成动作。

`Prompt` 是提示词模板或行为指导。它一般不是直接执行业务动作，而是给模型提供一段可复用的上下文或任务模板。比如“API 草稿箱能力说明”“代码评审规范”“数据分析 SQL 生成规范”等。

`Resource` 是可读取资源。它更像上下文材料或知识文件，比如开发规范文档、接口说明、业务说明、Markdown 文档等。Agent 可以读取 Resource 来补充背景知识。

可以这样理解：

```text
Tool     让 Agent 做事
Prompt   告诉 Agent 怎么想、怎么组织任务
Resource 给 Agent 提供背景材料
```

一句话总结：

> Tool 是动作，Prompt 是模板，Resource 是上下文；三者组合起来，才能让 Agent 既能理解规则，又能读取资料，还能调用业务能力。

## 架构设计连贯回答模板

如果面试官让你整体讲架构，可以这样说：

> 这个项目是一个多模块 Spring Boot MCP 服务平台。`bootstrap` 负责启动和聚合配置，`gateway` 负责 MCP 请求入口，包括 Token 鉴权、用户上下文构建、`tools/list` 过滤和 `tools/call` 动态工具拦截；`core` 负责核心业务模型，比如用户、角色、Token、动态工具、API 配置、审计日志和数据源；`tools` 负责注册内置 MCP 工具；`admin` 提供后台页面和 REST API，用来管理工具、Prompt、Resource、Token 和权限。
>
> 请求链路上，AI 客户端或自研 Agent 请求 `/mcp`，带上 `Authorization: Bearer mcp_xxx`。服务端先通过 `ApiKeyAuthFilter` 校验 token，查出用户、角色和工具权限，放到 `McpUserContext`。当客户端调用 `tools/list` 时，服务会根据当前 token 的工具选择和角色权限，只返回可用工具，并额外注入数据库里的动态工具。当客户端调用 `tools/call` 时，内置工具走 Spring AI 的 `@Tool` 方法，动态工具则由 `McpToolsCallFilter` 拦截，交给 `DynamicToolService` 加载 Groovy 脚本和关联 API 执行。
>
> 这个拆分的好处是协议入口、核心业务、工具实现和后台管理互相解耦。内置工具适合平台基础能力，动态工具适合业务 API 快速封装；Prompt 用来提供可复用提示词模板，Resource 用来提供上下文资料，Tool 用来执行具体动作。

## MCP 协议与项目实现追问

### Q1: 对外提供的是哪些接口？

严格来说，对外主要提供的是一个标准 MCP Server 端点：

```text
POST /mcp
```

客户端不是分别请求 `/tools/list`、`/tools/call`、`/prompts/list` 这些 REST 接口，而是统一请求 `/mcp`，请求体里通过 JSON-RPC method 区分具体动作。

比如获取工具列表：

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list",
  "params": {}
}
```

调用工具：

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/call",
  "params": {
    "name": "query_sales_order_by_id",
    "arguments": {
      "orderId": "16"
    }
  }
}
```

一句话总结：

> 对外暴露的是标准 MCP Server 端点 `/mcp`，`tools/list`、`tools/call` 这些是 MCP 协议里的 JSON-RPC method，不是普通 REST 路径。

### Q2: `/mcp` 端点是怎么暴露出来的？

`/mcp` 端点不是项目自己手写 `@PostMapping("/mcp")` 暴露的，而是通过 Spring AI MCP Server 自动注册的。

实现上主要有三步：

第一，引入 MCP Server WebMVC 依赖：

```xml
<artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
```

第二，在配置里开启 Streamable HTTP：

```yaml
spring:
  ai:
    mcp:
      server:
        version: 1.0.0
        protocol: STREAMABLE
```

第三，Spring Boot 启动后，Spring AI MCP Server 的自动配置会注册标准 MCP WebMVC 处理器，对外提供 `/mcp`。

项目自己做的是在这个标准端点外层增强，比如：

```text
SecurityConfig        要求 /mcp 必须认证
ApiKeyAuthFilter      校验 Bearer Token
McpToolsListFilter    拦截 tools/list 做权限过滤和动态工具注入
McpToolsCallFilter    拦截 tools/call 做动态工具执行
McpToolsConfig        注册 Java 内置 @Tool 工具
```

一句话总结：

> `/mcp` 是 Spring AI MCP Server starter 自动暴露的标准协议端点，项目在它外层加了鉴权、权限过滤、动态工具注入和动态工具执行。

### Q3: 客户端怎么知道有 `tools/list` 和 `tools/call`？

客户端知道这些方法，是因为它本身就是 MCP Client，内置实现了 MCP 协议。

MCP 的基本流程是：

```text
1. 客户端请求 initialize
2. 服务端返回 capabilities，声明自己支持 tools、prompts、resources 等能力
3. 客户端看到服务端支持 tools
4. 客户端按 MCP 协议调用 tools/list 获取工具清单
5. 模型选择工具后，客户端按 MCP 协议调用 tools/call 执行工具
```

也就是说，`tools/list` 和 `tools/call` 不是让客户端猜的，也不是我们自定义一套接口文档让客户端适配，而是 MCP 标准协议 method。

一句话总结：

> Cursor、Codex、Claude 或自研 Agent 作为 MCP Client，本身知道 MCP 标准 method；服务端只需要在 `initialize` 里声明支持 tools，客户端就会按协议调用 `tools/list` 和 `tools/call`。

### Q4: `capabilities` 是固定的吗？

能力名称大方向是 MCP 协议标准定义的，但服务端具体声明支持哪些能力，是由服务端实现决定的。

常见服务端能力包括：

```text
tools       工具能力
prompts     提示词模板能力
resources   资源读取能力
logging     日志能力
completions 补全能力
```

服务端可能返回：

```json
{
  "capabilities": {
    "tools": {
      "listChanged": true
    },
    "prompts": {
      "listChanged": true
    },
    "resources": {
      "subscribe": false,
      "listChanged": true
    },
    "logging": {},
    "completions": {}
  }
}
```

其中：

```text
tools       表示支持 MCP Tools
prompts     表示支持 MCP Prompts
resources   表示支持 MCP Resources
listChanged 表示列表变化时可以通知客户端重新拉取
subscribe   表示资源是否支持订阅更新
```

一句话总结：

> `tools`、`prompts`、`resources` 这些 capability 名称是 MCP 标准里的；具体支持哪些能力，以及 `listChanged`、`subscribe` 这些细节，是服务端在 `initialize` 阶段声明的。

### Q5: 标准 MCP 一般有哪些能力？

标准 MCP 可以分成基础生命周期、服务端能力、客户端能力和传输方式。

基础生命周期常见 method：

```text
initialize
notifications/initialized
ping
```

服务端最常见能力：

```text
tools/list
tools/call

prompts/list
prompts/get

resources/list
resources/read
resources/subscribe
resources/unsubscribe
```

其他常见能力：

```text
logging
completion
roots
sampling
elicitation
experimental
```

传输方式常见有：

```text
stdio            本地进程通信
streamable-http  HTTP 通信，适合远程服务和 Web 服务
```

这个项目使用的是：

```text
streamable-http
POST /mcp
```

一句话总结：

> MCP 最核心的是 `tools`、`prompts`、`resources` 三类能力，配合 `initialize` 做能力协商；传输上常见有 stdio 和 streamable-http，这个项目用的是 streamable-http。

### Q6: `tools/list` 在项目哪里实现？

`tools/list` 分两层。

第一层是 Spring AI MCP Server 自动实现的标准 `tools/list`。项目通过 `ToolCallbackProvider` 注册内置工具，Spring AI 会根据 `@Tool` 方法生成 MCP 工具列表。

内置工具注册位置：

```text
mcp-server-tools/src/main/java/com/bear/mcp/tools/config/McpToolsConfig.java
```

核心代码是：

```java
@Bean
public ToolCallbackProvider builtinTools() {
    return MethodToolCallbackProvider.builder()
            .toolObjects(...)
            .build();
}
```

第二层是项目自己写的 `tools/list` 响应增强和过滤逻辑。

位置：

```text
mcp-server-gateway/src/main/java/com/bear/mcp/gateway/filter/McpToolsListFilter.java
```

它会拦截 `POST /mcp`，判断 method 是否是 `tools/list`。然后先放行给 Spring AI 生成原始工具列表，再根据当前用户、Token 和角色权限过滤内置工具，并从数据库里的动态工具库加载动态工具，最终改写成当前 token 可用的工具列表。

一句话总结：

> Spring AI 负责生成标准内置工具列表，`McpToolsListFilter` 负责按用户、Token、角色权限过滤工具，并注入动态工具。

### Q7: `tools/call` 在项目哪里实现？

`tools/call` 也分两层。

第一层是内置工具调用，由 Spring AI MCP Server 自动实现。它会根据工具名路由到对应的 Java `@Tool` 方法。

内置工具注册位置：

```text
mcp-server-tools/src/main/java/com/bear/mcp/tools/config/McpToolsConfig.java
```

第二层是动态工具调用，由项目自己实现。

入口位置：

```text
mcp-server-gateway/src/main/java/com/bear/mcp/gateway/filter/McpToolsCallFilter.java
```

处理逻辑是：

```text
1. 拦截 POST /mcp
2. 判断 method 是否是 tools/call
3. 解析 params.name，得到工具名
4. 判断工具名是否是动态工具
5. 如果不是动态工具，放行给 Spring AI 原生处理
6. 如果是动态工具，交给 DynamicToolService 执行
```

动态工具真正执行位置：

```text
mcp-server-core/src/main/java/com/bear/mcp/core/service/DynamicToolService.java
```

Groovy 脚本执行位置：

```text
mcp-server-core/src/main/java/com/bear/mcp/core/groovy/GroovyScriptEngine.java
```

一句话总结：

> 内置工具的 `tools/call` 由 Spring AI 自动路由到 `@Tool` 方法；动态工具的 `tools/call` 由 `McpToolsCallFilter` 拦截后交给 `DynamicToolService` 和 `GroovyScriptEngine` 执行。

### Q8: 刚才这些问题属于什么类型？

这些问题主要分成两类：

第一类是 MCP 标准协议问题：

```text
initialize 是什么
capabilities 是什么
tools/list、tools/call 客户端怎么知道
标准 MCP 一般有哪些能力
Tool、Prompt、Resource 分别是什么
MCP 是统一 /mcp 端点加 JSON-RPC method，而不是多个 REST 接口
```

第二类是项目里的 MCP 落地实现问题：

```text
/mcp 是怎么暴露出来的
tools/list 在代码哪里实现
tools/call 在代码哪里实现
Spring AI MCP Server 自动做了什么
项目自己的 Filter 增强了什么
动态工具和内置工具怎么区分
Token、权限、动态工具怎么和 MCP 协议结合
```

一句话总结：

> 这类问题其实是在考两个点：一是是否理解 MCP 协议本身，二是是否清楚这个项目如何在 Spring AI MCP Server 基础上做企业级扩展。

## MCP 协议追问连贯回答模板

如果面试官围绕 MCP 协议连续追问，可以这样说：

> MCP 标准定义了 `initialize`、`tools/list`、`tools/call`、`prompts/list`、`resources/list` 等协议 method。客户端不是访问多个 REST 路径，而是统一请求 `/mcp`，通过 JSON-RPC method 区分具体动作。客户端作为 MCP Client，本身知道这些标准 method；服务端在 `initialize` 阶段通过 capabilities 声明自己支持 tools、prompts、resources 等能力，客户端再按协议调用对应的 list 和 call 方法。
>
> 我这个项目里的 `/mcp` 端点是通过 `spring-ai-starter-mcp-server-webmvc` 自动暴露的，配置 `spring.ai.mcp.server.protocol=STREAMABLE` 后，Spring AI 会注册标准 MCP Server WebMVC 处理器。项目在这个标准端点外层做企业级增强，比如通过 `ApiKeyAuthFilter` 做 Bearer Token 鉴权，通过 `McpToolsListFilter` 改写 `tools/list` 响应，根据 token 和角色权限返回可用工具，通过 `McpToolsCallFilter` 拦截动态工具调用，再交给 `DynamicToolService` 和 `GroovyScriptEngine` 执行业务脚本。

## 动态工具类

### Q1: 动态工具怎么创建？

动态工具不是写死在 Java 代码里的，而是通过后台页面或 MCP 内置管理工具创建出来的。

创建时主要会填写这些信息：

```text
toolName          MCP 调用展示名称，全局唯一
toolDescription   工具描述，帮助 Agent 判断什么时候使用
inputSchema       入参 JSON Schema，告诉客户端需要哪些参数
groovyScript      动态工具执行逻辑
linkedApiIds      关联的 API ID 列表，限制脚本可调用哪些 API
linkedDataSourceIds 关联的数据源 ID，限制脚本可查询哪些数据源
isPublic          是否公开
isEnabled         是否启用
```

这些信息会先存到动态工具草稿表：

```text
mcp_dynamic_tool
```

创建后可以在后台调试、修改、提交审批或发布到工具库。发布后的当前版本会进入：

```text
mcp_dynamic_tool_library
```

一句话总结：

> 动态工具本质上是把工具元信息、参数 schema、Groovy 脚本和关联 API 存到数据库里，再在 `tools/list` 和 `tools/call` 时动态加载。

### Q2: 不发版怎么新增 MCP Tool？

普通内置工具需要写 Java `@Tool` 方法，然后重新发布服务。动态工具的思路是不把工具逻辑固定在代码里，而是把工具定义存到数据库。

新增动态工具后，服务端不需要重启。客户端下一次调用 `tools/list` 时，`McpToolsListFilter` 会从数据库里读取当前用户和 token 可用的动态工具，把它们转换成 MCP Tool 格式返回。

也就是说，MCP 客户端看到的工具列表并不完全来自 Java 代码，还来自数据库里的动态工具库。

调用时也是一样。客户端发 `tools/call`，服务端根据工具名判断这是动态工具，就从数据库加载脚本和配置执行。

一句话总结：

> 不发版新增 MCP Tool 的关键，是把工具定义从代码注册改成数据库配置，`tools/list` 动态发现，`tools/call` 动态执行。

### Q3: Groovy 脚本怎么执行？

动态工具调用时，服务端会进入 `DynamicToolService.executeTool()`。

大致流程是：

```text
1. 根据 toolName 查询 mcp_dynamic_tool_library
2. 校验工具是否存在、是否启用
3. 校验当前用户是否有权限访问
4. 解析 linkedApiIds、linkedDataSourceIds
5. 构建 ScriptContext
6. 调用 GroovyScriptEngine 执行脚本
7. 把执行结果包装成 MCP JSON-RPC 响应
8. 异步记录审计日志
```

脚本执行时会注入受控上下文变量：

```text
params      MCP 调用传入的参数
userId      当前用户 ID
userName    当前用户名
toolName    当前工具名
log         受控日志对象
runRequest  受控 API 调用器
runSql      受控数据源查询器
```

比如订单查询工具的脚本可以理解成：

```groovy
return runRequest.runRequest("API0000000023", params)
```

`GroovyScriptEngine` 会用线程池执行脚本，并设置超时时间，避免脚本长时间阻塞请求线程。

一句话总结：

> Groovy 脚本是在服务端受控上下文里执行的，脚本不能随便拿系统能力，而是通过平台注入的 `runRequest`、`runSql` 等对象调用被授权的 API 和数据源。

### Q4: 动态工具怎么关联 API？

API 配置本身存在：

```text
mcp_request_config
```

每条 API 配置有一个唯一的 `request_id`，比如：

```text
API0000000023
```

动态工具通过 `linked_api_ids` 字段关联 API，这个字段是一个 JSON 数组：

```json
["API0000000023"]
```

这个字段存在两处：

```text
mcp_dynamic_tool.linked_api_ids          草稿工具关联的 API
mcp_dynamic_tool_library.linked_api_ids  已发布工具关联的 API
```

脚本执行时，`runRequest` 会检查调用的 API 是否在 `linkedApiIds` 列表中。只有提前关联过的 API 才能调用。

这样可以避免动态脚本随意调用系统里任意 API。

一句话总结：

> API 本体在 `mcp_request_config`，动态工具通过 `linked_api_ids` 白名单关联 API，脚本里的 `runRequest` 只能调用这个白名单里的 API。

### Q5: 为什么用 Groovy，不直接用 Java？

Java 适合稳定的基础能力，比如内置工具、权限、审计、网关链路。但如果每新增一个业务工具都写 Java，就必须改代码、编译、测试、发版，迭代成本比较高。

Groovy 的优势是运行在 JVM 上，和 Java 项目集成成本低，可以直接在服务端动态编译执行。对于轻量的 API 编排、参数转换、结果处理，Groovy 比较灵活。

比如一个动态工具可能只是：

```text
接收 orderId
调用订单查询 API
提取关键字段
返回给 Agent
```

这种逻辑如果每次都写 Java 发版，会比较重。用 Groovy 可以让运营或开发在后台配置后快速发布。

但 Groovy 不是没有代价。动态脚本需要安全控制，比如限制危险类、限制调用范围、加超时、审计、审批和版本管理。

一句话总结：

> Java 负责稳定平台能力，Groovy 负责轻量动态编排；选择 Groovy 是为了不发版扩展工具能力，但必须配套安全沙箱、权限和审计。

### Q6: 如果脚本写错了怎么办？

脚本错误分几类处理。

第一类是保存或发布前的校验。创建或更新动态工具时，会先调用脚本校验逻辑，检查 Groovy 语法和安全限制。如果语法不对，或者使用了被禁止的类和方法，就不允许保存或发布。

第二类是运行时异常。比如参数缺失、API 返回异常、脚本逻辑空指针等，这类错误会在 `GroovyScriptEngine` 执行时被捕获，转换成失败结果返回给 MCP 客户端。

第三类是超时。脚本执行在线程池里跑，并设置执行超时时间，超过时间会返回超时错误，避免拖垮服务。

第四类是审计。无论成功还是失败，都会记录工具名、参数摘要、响应摘要、状态、错误信息、耗时、用户、session、IP 等审计信息，方便后续排查。

一句话总结：

> 脚本写错时，保存阶段尽量拦截语法和安全问题，运行阶段捕获异常和超时，并通过 MCP 错误响应和审计日志暴露问题。

### Q7: 动态工具怎么灰度、版本管理、上下架？

动态工具有草稿表、工具库主表和版本历史表，分别对应不同阶段。

常见表包括：

```text
mcp_dynamic_tool          草稿表，用户创建和编辑自己的工具
mcp_dynamic_tool_library  工具库主表，保存当前发布版本
mcp_dynamic_tool_version  历史版本表，保存归档版本
mcp_tool_approval_log     工具审批日志
```

一般流程是：

```text
1. 用户在草稿箱创建或修改动态工具
2. 本地调试脚本和参数
3. 提交审批，填写版本号和变更说明
4. 审批通过后发布到工具库主表
5. 旧版本归档到版本历史表
6. 用户或 token 选择该工具后，MCP tools/list 才会暴露
7. 下架或禁用后，tools/list 不再返回，tools/call 也不能执行
```

灰度可以通过 token 级工具选择来做。比如先只给某个测试 token 选择新工具，确认没问题后再开放给更多用户或角色。

也可以通过公开状态和启用状态控制范围：

```text
is_public   控制是否公开
is_enabled  控制是否启用
role_tool   控制角色是否有工具权限
user_tool_selection 控制某个 token 是否选择该工具
```

一句话总结：

> 动态工具通过草稿、审批、发布、版本归档、启停状态和 token 级选择实现生命周期管理；灰度可以通过角色权限和 token 选择控制曝光范围。

## 动态工具连贯回答模板

如果面试官让你讲动态工具，可以这样说：

> 动态工具是这个项目的核心亮点。普通 MCP Tool 通常要在代码里写 `@Tool` 方法，新增工具需要发版；我这里把工具名称、描述、参数 schema、Groovy 脚本、关联 API ID、关联数据源 ID 存到数据库里。客户端调用 `tools/list` 时，服务会从动态工具库读取当前 token 可用的工具，动态组装成 MCP Tool 返回；调用 `tools/call` 时，如果工具名是动态工具，就从数据库加载脚本和配置执行。
>
> 脚本执行时不是完全开放的，而是通过 `GroovyScriptEngine` 构建受控上下文，只注入 `params`、`userId`、`runRequest`、`runSql` 等对象。`runRequest` 只能调用动态工具提前关联过的 API ID，API 本体存储在 `mcp_request_config`，动态工具通过 `linked_api_ids` 做白名单绑定。这样既能支持轻量编排，又能避免脚本随意调用内部接口。
>
> 生命周期上，动态工具先进入草稿表，调试后提交审批，发布后进入工具库主表，旧版本归档到版本历史表。通过 `is_enabled`、`is_public`、角色工具权限和 token 级工具选择控制上线、下线和灰度范围。这样可以做到不发版新增 MCP Tool，同时保留权限、审计和版本管理。

## 安全类

### Q1: Token 怎么校验？

MCP 客户端请求 `/mcp` 时，会在 Header 中带上 Bearer Token：

```text
Authorization: Bearer mcp_xxx
```

服务端在 `ApiKeyAuthFilter` 中提取 token，调用 `TokenService` 校验 token 是否存在、是否有效、是否属于某个用户。校验通过后，会查询用户信息、角色、工具权限，并构建 `McpUserContext` 放到当前请求线程上下文中。

上下文里会包含：

```text
userId
tokenId
roles
allowedTools
sessionId
clientIp
userName
```

后续 `tools/list`、`tools/call`、Prompts、Resources 都依赖这个上下文做权限过滤和审计。

一句话总结：

> Token 校验是在 MCP 请求入口统一完成的，校验通过后把用户、角色、tokenId 和权限放到 `McpUserContext`，后续所有 MCP 能力都基于这个上下文执行。

### Q2: 不同用户怎么隔离工具？

工具隔离主要靠三层控制：

```text
Token 鉴权       确认请求属于哪个用户、哪个 token
RBAC 角色权限    控制用户理论上能访问哪些工具
Token 工具选择   控制这个 token 实际暴露哪些工具
```

角色权限是粗粒度授权，比如某个角色可以使用订单查询、日志查询、API 配置工具等。

Token 工具选择是细粒度暴露控制。即使用户角色有某个工具权限，如果这个 token 没有选择该工具，`tools/list` 也不会返回它。

一句话总结：

> 用户隔离不是只靠 token，而是 Token 身份、RBAC 角色权限、token 级工具选择三层共同控制。

### Q3: Agent 能不能越权调用别人的工具？

正常情况下不能。

第一，Agent 每次请求都必须带 Bearer Token，服务端会根据 token 找到当前用户和 tokenId。

第二，`tools/list` 返回时只返回当前 token 可见的工具。Agent 理论上看不到没权限的工具。

第三，即使 Agent 猜到了某个工具名，直接发 `tools/call`，动态工具执行时仍会在 `DynamicToolService` 中校验工具是否启用、当前用户是否可访问，以及角色权限和工具可见范围。

第四，动态工具内部调用 API 时，`runRequest` 还会校验调用的 API 是否在该工具的 `linked_api_ids` 白名单里。

一句话总结：

> Agent 不能只靠猜工具名越权，因为服务端在工具发现、工具调用和工具内部 API 调用三个阶段都做了权限校验。

### Q4: 动态脚本有没有安全风险？

有，而且这是必须主动承认的风险点。

动态脚本的风险包括：

```text
执行危险 Java 类或系统命令
访问本地文件或环境变量
发起任意内网 HTTP 请求
执行耗时死循环拖垮服务
绕过平台 API 白名单
通过 runSql 查询敏感数据
日志或审计中泄露敏感参数
```

当前项目做了几类控制：

```text
Groovy AST 安全限制
禁止 Runtime、System、File、ProcessBuilder 等危险类
禁止反射、进程执行等危险方法
脚本执行超时
线程池隔离
runRequest API 白名单
runSql 数据源白名单
审计日志记录调用过程
```

但动态脚本天然有风险，后续还应该继续收紧，比如禁用或限制裸 `http.get/post`，让所有外部请求都走 `mcp_request_config` 白名单。

一句话总结：

> 动态脚本是能力扩展点，也是风险扩展点，所以必须用白名单、超时、线程池隔离、禁用危险类、限制外部请求和审计日志一起兜住。

### Q5: `runSql` 会不会被注入？

`runSql` 本身确实存在 SQL 风险，因为它允许脚本执行 SQL。项目当前主要通过调用范围和 SQL 类型限制来降低风险。

第一，动态工具必须提前关联数据源 ID，脚本只能调用 `linkedDataSourceIds` 里的数据源，不能随便连任意数据库。

第二，查询类能力通常限制为单条 SELECT，不允许分号，不允许执行多语句。

第三，写操作需要走单独的执行入口，并禁止 `DROP`、`TRUNCATE`、`ALTER`、`CREATE`、`GRANT`、`REVOKE` 等高危语句。

第四，所有调用都应记录审计日志，包括用户、工具名、参数摘要、耗时和状态。

但如果脚本作者把用户输入直接拼接进 SQL，仍然可能有注入风险。更理想的做法是提供参数化 SQL 能力，或者把常用数据查询封装成更高层的受控工具，减少直接写 SQL 的场景。

一句话总结：

> `runSql` 当前通过数据源白名单、SQL 类型限制和审计降低风险，但拼接 SQL 仍可能注入，后续可以引入参数化查询和更严格的 SQL parser。

### Q6: `runRequest` 会不会调用任意内部接口？

正常情况下不会。

`runRequest` 不是传一个 URL 就能调用，而是调用平台里已经配置好的 API。API 配置存在：

```text
mcp_request_config
```

动态工具通过 `linked_api_ids` 绑定允许调用的 API：

```json
["API0000000023"]
```

脚本执行时，如果调用：

```groovy
runRequest.runRequest("API0000000023", params)
```

服务端会检查 `API0000000023` 是否在当前工具的 `linkedApiIds` 里。如果不在，就拒绝调用。

一句话总结：

> `runRequest` 走的是 API 配置白名单，不是任意 URL 调用；动态工具只能调用自己提前关联过的 API ID。

### Q7: 如果有人写死 `http.get("内网地址")` 怎么办？

这是当前设计里需要重点说明的风险。

项目里给 Groovy 脚本注入了一个简单 HTTP 客户端 `http`，理论上脚本可以写：

```groovy
http.get("http://internal.xxx")
```

这会带来 SSRF 或内网探测风险。当前主要依赖脚本审批、权限控制、审计和 Groovy 安全限制来降低风险，但这还不够彻底。

更好的改进方案是：

```text
禁用裸 http 对象
或者只允许 http 访问域名白名单
所有外部请求统一走 runRequest
runRequest 只允许调用 mcp_request_config 中配置过的 API
对 API 配置做审批、分类、限流和审计
```

面试时不要装作没有风险，可以主动说：

> 这个点我认为后续需要收紧。动态脚本里裸 `http.get/post` 灵活但风险较高，生产上更推荐禁用裸 HTTP，让所有外部请求都走 API 配置白名单，也就是 `runRequest`。

一句话总结：

> 裸 `http.get` 是动态脚本里的安全风险点，生产级方案应该限制或禁用，统一走 `runRequest` 白名单。

### Q8: 审计日志记录哪些内容？

审计日志的目标是回答几个问题：

```text
谁调用了
用哪个 token 调用
什么时候调用
从哪个 IP 调用
调用了哪个工具
传了什么参数
返回了什么结果摘要
成功还是失败
耗时多久
失败原因是什么
属于哪类工具
```

项目里动态工具执行后会记录：

```text
toolName
requestParams
responseSummary
status
errorMessage
durationMs
toolType
userId
userName
sessionId
clientIp
```

参数和响应一般做摘要或截断，避免审计表过大，也避免敏感信息完整落库。

一句话总结：

> 审计日志记录调用主体、工具、参数摘要、结果摘要、状态、错误、耗时、session 和 IP，用于追踪 Agent 调用了什么企业能力。

## 安全类连贯回答模板

如果面试官追安全，可以这样回答：

> 当前安全模型主要是 Token + RBAC + token 级工具选择。MCP 请求进来后，`ApiKeyAuthFilter` 校验 Bearer Token，构建 `McpUserContext`；`tools/list` 只返回当前 token 选择且角色有权限的工具；`tools/call` 执行动态工具时还会校验工具是否启用、当前用户是否可访问。动态工具内部的 `runRequest` 只能调用提前关联过的 API ID，`runSql` 只能使用提前绑定的数据源。
>
> 但动态脚本天然有风险，所以我做了 Groovy AST 安全限制、禁用危险类和危险方法、脚本超时、线程池隔离、API 白名单、数据源白名单和审计日志。这里我也会主动承认一个风险点：脚本里裸 `http.get/post` 灵活但可能带来 SSRF 风险，生产上应该进一步收紧，让所有外部请求都走 `mcp_request_config` 白名单和 `runRequest`，并对 API 配置做审批、限流和审计。

## 权限模型类

### Q1: 用户、角色、Token、Tool 之间是什么关系？

整体关系可以这样理解：

```text
用户 User
  ↓ 拥有
角色 Role
  ↓ 授权
工具 Tool 权限

用户 User
  ↓ 创建
Token
  ↓ 选择
实际暴露给 MCP 客户端的 Tool
```

角色决定用户理论上能使用哪些工具，Token 决定某个 MCP 客户端实际能看到哪些工具。

比如用户有订单查询和日志查询权限，但他可以给 Cursor 创建一个 token，只选择订单查询；再给 Codex 创建另一个 token，只选择日志查询和 API 管理。

相关表可以记住：

```text
mcp_user                 用户
mcp_role                 角色
mcp_user_role            用户角色关系
mcp_role_tool            角色工具权限
mcp_user_token           用户 Token
mcp_user_tool_selection  token 选择的工具
```

一句话总结：

> 角色控制用户能不能用，Token 控制某个客户端实际暴露哪些工具。

### Q2: 为什么工具选择绑定 Token，而不是绑定用户？

因为一个用户可能有多个 AI 客户端或多个使用场景。

如果工具选择只绑定用户，那这个用户所有客户端看到的工具都一样，权限暴露面会比较大。

绑定到 Token 后，可以做到更细粒度控制：

```text
Cursor token       只给研发常用工具
Codex token        给代码辅助和项目分析工具
自研 Agent token   只给数据分析相关工具
测试 token         只给灰度中的新工具
```

这样即使某个 token 泄露，也只影响这个 token 暴露的工具范围，不会暴露用户所有工具能力。

一句话总结：

> 工具选择绑定 Token，是为了按客户端、按场景做最小权限暴露，而不是让用户所有工具能力一次性暴露给所有 Agent。

### Q3: 一个用户多个 Token 有什么意义？

一个用户多个 Token 可以支持多客户端、多环境、多场景隔离。

常见场景：

```text
Cursor 一个 token
Codex 一个 token
Claude 一个 token
自研 Agent 一个 token
测试环境一个 token
生产环境一个 token
```

每个 token 可以配置不同工具集合。比如：

```text
研发排障 token：日志查询、订单查询、支付查询
数据分析 token：数据源查询、指标分析、报表工具
管理后台 token：API 配置、Prompt 配置、动态工具配置
```

这有几个好处：

```text
权限最小化
风险隔离
方便吊销单个客户端
方便审计区分来源
方便灰度新工具
```

一句话总结：

> 多 Token 的意义是把同一个用户在不同客户端、不同场景下的 MCP 能力隔离开，方便最小权限、审计和吊销。

### Q4: MCP 客户端如何做到只看到自己的工具？

客户端请求 `tools/list` 时必须带 token。

服务端根据 token 查出：

```text
当前用户
当前 tokenId
用户角色权限
这个 token 选择的工具列表
```

然后在 `McpToolsListFilter` 中只返回同时满足条件的工具：

```text
角色有权限
token 已选择
工具已启用
动态工具当前用户可访问
```

所以不同 MCP 客户端即使用的是同一个用户，只要 token 不同，看到的工具列表也可以不同。

一句话总结：

> MCP 客户端看到什么工具，不是客户端决定的，而是服务端根据 tokenId、用户角色和工具选择动态过滤出来的。

## 权限模型连贯回答模板

如果面试官问权限模型，可以这样说：

> 我的权限模型分两层：第一层是 RBAC，用户绑定角色，角色授权工具，决定用户理论上可以使用哪些能力；第二层是 token 级工具选择，决定某一个 MCP 客户端实际能看到哪些工具。这样一个用户可以给 Cursor、Codex、自研 Agent 分别创建不同 token，每个 token 只暴露当前场景需要的工具。
>
> 客户端调用 `tools/list` 时，服务端根据 Bearer Token 找到 tokenId 和 userId，再查用户角色允许的工具以及该 token 选择的工具，最终只返回交集。这样做的好处是最小权限、风险隔离、方便审计和方便单独吊销某个客户端 token。

## Agent 选工具类

### Q1: 如果有多个相似工具，Agent 怎么选？

Agent 选工具主要依赖 MCP Tool 暴露出来的三类信息：

```text
name         工具名称
description  工具描述
inputSchema  参数结构和参数说明
```

其中 `description` 非常关键。它不只是给人看的说明，也是给模型判断工具适用场景的依据。

比如有几个相似工具：

```text
query_sales_order_by_id     查询普通销售订单
query_refund_order_by_id    查询退款单
query_success_order_by_id   查询支付成功订单
```

工具描述里应该明确写清楚：

```text
query_sales_order_by_id:
默认订单详情查询。用户只说“查询订单”时优先使用。

query_refund_order_by_id:
仅用于查询退款单。用户明确提到退款、退费、refund 时使用。

query_success_order_by_id:
仅用于查询支付成功订单。用户明确提到已支付、支付成功、成交订单时使用。
```

如果用户意图明确，Agent 直接选对应工具。如果用户意图不明确，应该优先选择通用工具，或者反问用户。

一句话总结：

> Agent 选工具不是靠魔法，而是靠工具名、描述和参数 schema。相似工具要在描述里写清楚适用场景和排他条件。

### Q2: `tools/list` 返回太多工具怎么办？

工具太多会带来两个问题：

```text
模型上下文变长
工具选择准确率下降
```

解决思路不是把所有工具都暴露给 Agent，而是做分层和收敛。

当前项目已经做了几层收敛：

```text
RBAC 角色权限        用户没有角色权限的工具不返回
Token 工具选择       当前 token 没选择的工具不返回
动态工具启用状态      禁用或下架的工具不返回
用户可见性           私有工具只有创建者可见，公开工具按权限可见
```

后续如果工具规模继续增大，可以继续做：

```text
按场景创建 token，比如排障 token、数据分析 token、运营 token
按工具分类返回，比如订单类、支付类、日志类、配置类
引入工具搜索或工具路由，先召回少量候选工具
把多个细碎工具合并成一个更通用的工具
对低频工具不默认暴露，需要时再加载
```

一句话总结：

> `tools/list` 工具太多时，要通过角色、token、分类、场景和检索做工具收敛，避免把所有企业能力一次性塞给 Agent。

### Q3: 工具描述怎么写才稳定？

稳定的工具描述要解决三个问题：

```text
这个工具做什么
什么时候应该用
什么时候不应该用
```

推荐格式：

```text
工具用途：一句话说明工具能做什么
适用场景：用户出现哪些意图或关键词时使用
不适用场景：哪些相似问题不要用这个工具
参数说明：每个参数是什么、格式、是否必填、示例
返回说明：返回的大概内容和注意事项
```

比如订单查询工具：

```text
根据销售订单 ID 查询订单详情。用户只说“查询订单”“查销售订单”“订单详情”时优先使用。
如果用户明确提到退款单、退费单，不要使用本工具，应使用退款单查询工具。
参数 orderId 为销售订单 ID，例如 16。
```

描述里尽量不要写模糊话，比如“查询相关信息”“处理订单能力”。这种描述会让模型难以区分工具。

一句话总结：

> 好的工具描述要写清楚用途、触发条件、排除条件和参数示例，尤其是相似工具之间的边界。

### Q4: 用户说“查订单”，但有退款订单、成功订单、普通订单工具，怎么办？

这要看有没有通用订单查询工具。

如果有通用工具，比如：

```text
query_order_by_id
query_sales_order_by_id
```

用户只说“查订单”时，优先调用通用订单查询工具。查到基础订单后，再根据返回的支付状态、退款状态决定是否继续调用退款工具或支付工具。

如果没有通用工具，只有退款订单和成功订单两个专用工具，而用户没有说清楚，就应该反问：

```text
你要查的是普通销售订单、支付成功订单，还是退款订单？
```

不建议在意图不明确时直接猜退款或成功订单，因为这会导致错误调用。

一句话总结：

> 用户意图不明确时，优先通用工具；没有通用工具就反问；不要在退款、成功、普通订单之间硬猜。

## Agent 选工具连贯回答模板

如果面试官问 Agent 怎么选工具，可以这样说：

> Agent 选工具主要依赖 MCP 返回的工具名、description 和 inputSchema。所以动态工具的描述要写得非常清楚，尤其是相似工具之间的边界，比如普通订单、退款订单、支付成功订单分别在什么场景下使用。如果用户意图明确，就选对应工具；如果用户只说“查订单”，我会优先设计一个通用订单查询工具作为默认入口，查到订单基础信息后再决定是否调用退款或支付工具。如果没有通用工具，就应该反问用户，而不是让 Agent 硬猜。
>
> 工具数量太多时，也不能全部暴露给 Agent。这个项目通过 RBAC、token 级工具选择、工具启停状态和私有/公开范围来控制 `tools/list` 返回结果。后续规模更大时，可以按场景 token、工具分类、关键词检索或工具路由进一步收敛候选工具，提高选择准确率。

## 性能和稳定性类

### Q1: 并发 `tools/call` 怎么处理？

`tools/call` 分内置工具和动态工具。

内置工具本质上是普通 Spring Web 请求，由 Tomcat 工作线程处理，调用对应 Java `@Tool` 方法。

动态工具会进入 `McpToolsCallFilter` 和 `DynamicToolService`，然后交给 `GroovyScriptEngine` 执行。Groovy 脚本不是直接无限制创建线程，而是放到专门的脚本线程池里执行。

线程池可以配置：

```yaml
mcp:
  groovy:
    executor:
      core-pool-size: 0
      max-pool-size: 0
      queue-capacity: 1000
```

这样可以隔离动态脚本执行，避免脚本执行过慢直接拖垮 Web 请求线程。

一句话总结：

> 并发 `tools/call` 由 Web 容器承接请求，动态脚本再进入独立 Groovy 执行线程池，用线程池和队列做隔离。

### Q2: Groovy 脚本执行超时怎么办？

Groovy 脚本执行时会通过 `Future.get(timeout)` 等待结果，超过时间会返回超时失败。

当前默认超时时间是固定值，动态工具执行时一般设置为 30 秒左右。超时后不会一直等脚本返回，而是构造失败结果返回给 MCP 客户端。

超时需要记录审计日志，包括：

```text
工具名
用户
参数摘要
超时时间
耗时
错误信息
```

后续可以进一步优化：

```text
按工具配置不同超时时间
超时后尽量 cancel future
对高耗时工具做限流
监控脚本线程池队列长度
```

一句话总结：

> Groovy 脚本通过线程池和超时控制执行，超时后返回失败结果并记录审计，避免单个脚本长期占用资源。

### Q3: API 调用失败怎么返回？

动态工具调用内部 API 通常通过 `runRequest` 走 `RequestConfigService`。

API 调用失败可能有几类：

```text
连接超时
读取超时
HTTP 状态码非 2xx
返回体解析失败
接口业务错误
请求配置不存在
未关联该 API
```

处理上应该统一转换成工具执行失败结果，最终按 MCP `tools/call` 响应返回给客户端。对于 Agent 来说，它能看到明确的错误信息，而不是请求一直挂住或服务端 500。

同时要记录审计日志，方便定位是哪个工具、哪个 API、哪个用户、什么参数导致失败。

一句话总结：

> API 调用失败要转换成标准工具失败结果返回给 MCP 客户端，同时记录审计和耗时，避免异常直接击穿服务。

### Q4: Redis/MySQL 挂了怎么办？

MySQL 是核心依赖，因为用户、Token、权限、动态工具、API 配置都在数据库里。如果 MySQL 挂了，MCP 工具发现、鉴权和动态工具执行都会受影响。

Redis 主要用于 session 等状态存储。如果 Redis 挂了，管理后台登录态、会话能力可能受影响，MCP token 鉴权是否受影响取决于具体实现是否依赖 Redis。

当前可以做的稳定性措施：

```text
连接池配置
健康检查
超时控制
失败快速返回
管理后台和 MCP 接口错误提示区分
关键配置可考虑缓存
```

更进一步可以做：

```text
MySQL 主从或高可用
Redis 高可用
工具列表和权限短期缓存
API 配置本地缓存
熔断和降级
启动时预热关键配置
```

一句话总结：

> MySQL 是强依赖，Redis 是会话类依赖；高可用方案要结合连接池、健康检查、缓存、超时、熔断和主从部署来做。

### Q5: 审计日志同步写还是异步写？

审计日志建议异步写。

原因是审计日志不应该阻塞主调用链。`tools/call` 的核心是尽快返回工具执行结果，如果每次都同步写审计，数据库抖动会直接影响 Agent 体验。

项目里可以通过 `@Async` 或独立线程池写审计日志。主链路只负责提交审计任务，审计失败时记录日志，不影响工具正常返回。

但异步写也要注意：

```text
线程池队列不能无限大
审计失败要有错误日志
重要场景可以考虑消息队列
敏感参数要脱敏或截断
```

一句话总结：

> 审计日志适合异步写，避免拖慢 `tools/call` 主链路，但要控制线程池、失败日志和敏感数据脱敏。

### Q6: 如何限流？

限流可以分多层做。

当前 API 配置里有：

```text
rate_limit_per_minute
```

可以对某个 API 配置做每分钟调用限制。

更完整的限流模型可以包括：

```text
按 token 限流
按 userId 限流
按 toolName 限流
按 request_id 限流
按 datasource_id 限流
按 IP 限流
```

技术实现上可以用：

```text
Redis 计数器
令牌桶
滑动窗口
本地限流 + Redis 全局限流
```

对于动态工具，还可以限制：

```text
单工具最大并发
脚本线程池队列长度
单次执行超时时间
单次返回结果大小
```

一句话总结：

> 限流不能只做接口层，还要按 token、用户、工具、API、数据源多个维度控制，动态工具还要控制并发、超时和返回大小。

### Q7: 如何监控工具调用耗时？

工具调用耗时可以从几层监控。

第一层是审计日志。每次工具调用记录 `durationMs`，可以按工具、用户、状态统计耗时。

第二层是应用指标。通过 Spring Boot Actuator 暴露健康检查和 metrics，例如线程池、请求耗时、错误率等。

第三层是动态脚本线程池监控。重点看：

```text
activeCount
poolSize
queueSize
completedTaskCount
rejectedCount
```

第四层是外部 API 调用监控。记录每个 `request_id` 的调用耗时、成功率、超时率。

更完整可以接 OpenTelemetry，把一次 Agent 调用链路串起来：

```text
MCP tools/call
  -> 动态工具执行
  -> runRequest API 调用
  -> 内部业务服务
```

一句话总结：

> 监控工具调用耗时可以从审计日志、Actuator 指标、脚本线程池、API 调用耗时和链路追踪几个层面做。

## 性能稳定性连贯回答模板

如果面试官问性能和稳定性，可以这样说：

> 性能稳定性上，我主要从超时、线程池隔离、异步审计、限流和监控几个方向处理。普通内置工具由 Spring Web 请求线程执行，动态工具会进入独立的 Groovy 脚本线程池，避免脚本执行拖垮 Web 线程。脚本执行有超时控制，API 配置里也有连接超时和读取超时，失败会转换成工具失败结果返回给 MCP 客户端，并记录审计日志。
>
> 审计日志适合异步写，避免影响 `tools/call` 主链路。限流上可以按 token、用户、工具、API、数据源等维度做控制，动态工具还要限制最大并发、队列长度和返回大小。监控上可以通过审计日志统计工具耗时和错误率，通过 Actuator 看应用指标，通过线程池指标看 Groovy 执行压力，后续还可以接 OpenTelemetry 做完整调用链追踪。
