# Ops Mid-Platform（运营中台 MCP Server）
http://118.178.255.26/
生产级运营中台 MCP 服务。基于 Spring AI MCP Server 构建的统一能力网关，将数据源、动态工具、请求配置、Prompt 模板、资源与技能统一纳管，并通过 MCP 协议对外提供标准化的工具调用能力。内置管理员控制台与共享 Studio，支撑运营侧 Agent 工具链的快速接入与安全管控。

## 核心能力

- **MCP 网关**：基于 Spring AI MCP Server（WebMVC / STREAMABLE 协议），提供 `initialize`、`tools/list`、`tools/call`、`resources`、`prompts` 全链路，Bearer Token 鉴权 + ApiKey 校验双层入口。
- **动态工具引擎**：Groovy 脚本即配即用，脚本运行于安全沙箱（类/方法白名单、字节码校验），支持 `runRequest`（白名单请求配置）、`runRedis`（权限受限的 Redis 操作）、`runSql`（外部数据源查询）等内置能力。
- **请求配置白名单**：HTTP 请求模板集中管理，支持 `{{key}}` 占位符动态注入与 Header/Params/Body 编排，天然适配飞书等第三方 Open API。
- **Redis 权限管控**：每个动态工具声明精确的 key、命令与 Hash 字段白名单，越权操作在沙箱层被拦截，防止脚本滥用。
- **外部数据源管理**：MySQL 数据源动态注册与连接池管理，SQL 工具按用户授权范围执行，敏感结果脱敏。
- **Prompt 模板与运行时**：Prompt 模板版本化管理与权限隔离，支持模板市场（Studio）与运行时工具联动。
- **技能（Skill）管理**：Markdown 技能包上传、发布、安装脚本生成与下载，支持 `skills/` 目录静态技能与动态技能市场。
- **资源管理**：资源上传预签名、下载预签名，对接火山引擎 TOS 对象存储。
- **共享 Studio 与社区**：工具 / Prompt / 资源 / 技能 / API 的发布、调试、分享与社区点赞互动。
- **审计与安全**：全链路操作审计日志、Token 生命周期管理、JWT 管理端认证、数据加密（Token 明文 / 数据源密码 AES 加密）、敏感值脱敏。
- **管理员控制台**：`admin-web` 提供数据源、动态工具、请求配置、Prompt、资源、技能、角色权限、Token、审计等全量管理界面。

## 技术栈

| 分类 | 选型 |
| --- | --- |
| 语言 / 运行时 | Java 17 |
| 框架 | Spring Boot 3.3、Spring AI 1.1（MCP Server WebMVC） |
| 持久层 | MyBatis 3、MySQL 8、HikariCP |
| 缓存 | Redis（Spring Data Redis） |
| 脚本引擎 | Apache Groovy 4（安全沙箱） |
| 安全 | Spring Security Crypto（BCrypt / JWT）、API Key 网关过滤器 |
| 对象存储 | 火山引擎 TOS SDK |
| HTTP 客户端 | OkHttp 4 |
| 工具库 | Hutool |

## 目录结构

```
ops-mid-platform
├── admin-web/                      # 管理员控制台前端（Vue）
├── docs/                           # 数据库初始化与迁移脚本、业务词表
│   ├── mysql-init.sql              # 建库建表 + 种子数据
│   └── mysql-migrate-*.sql         # 分功能增量迁移
├── skills/                         # 内置技能（Markdown 技能包）
├── src/main/java/com/ops/midplatform/
│   ├── admin/                      # 管理端：认证、控制台、数据源、工具、Prompt、资源、角色、Token、审计
│   ├── gateway/                    # MCP 网关过滤器（鉴权、tools/list、tools/call、resources、prompts）
│   ├── core/                       # 核心引擎：动态工具、Groovy 沙箱、请求配置、Redis 权限、数据源、Prompt、资源、技能、审计
│   ├── share/                      # 共享 Studio：工具/Prompt/资源/技能/API 发布与调试、社区
│   ├── publicapi/                  # 面向外部的公开接口（技能 CLI 等）
│   └── common/                     # 统一响应、异常、上下文
├── src/main/resources/
│   ├── application.yml             # 主配置（全部通过环境变量注入）
│   ├── mapper/                     # MyBatis XML
│   └── ops-skill/                  # 内置技能包资源
├── setup-db.ps1                    # Windows 本地建库脚本
└── pom.xml
```

## 快速开始

### 环境依赖

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6+

### 1. 初始化数据库

```bash
mysql -u root -p -e "create database if not exists ops_mid_platform default charset utf8mb4"
mysql -u root -p ops_mid_platform < docs/mysql-init.sql
```

按需执行 `docs/mysql-migrate-*.sql` 中的增量脚本（飞书工具、数据源工具、Prompt/技能/资源 Studio 等）。

### 2. 配置环境变量

所有连接信息与密钥均通过环境变量注入，禁止硬编码生产凭据：

```powershell
$env:BEAR_DB_URL       = 'jdbc:mysql://<host>:3306/ops_mid_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai'
$env:BEAR_DB_USERNAME  = '<db-user>'
$env:BEAR_DB_PASSWORD  = '<db-password>'
$env:BEAR_REDIS_HOST   = '127.0.0.1'
$env:BEAR_REDIS_PORT   = '6379'
$env:BEAR_REDIS_PASSWORD = '<redis-password>'
$env:BEAR_ADMIN_JWT_SECRET  = '<强随机密钥，建议 64 位以上>'
$env:BEAR_ADMIN_DATA_SECRET = '<独立数据加密密钥，建议与 JWT 密钥分离>'
```

### 3. 启动

```bash
mvn spring-boot:run
```

默认监听 `8090`，MCP 端点：

```text
http://localhost:8090/mcp
```

默认开发 Token：`mcp_dev_token`（生产环境务必通过管理端重建 Token 并撤销默认值）。

## 环境变量

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `BEAR_DB_URL` | `jdbc:mysql://127.0.0.1:3306/bear-mcp-single?...` | MySQL 连接串 |
| `BEAR_DB_USERNAME` / `BEAR_DB_PASSWORD` | `root` / `1234` | MySQL 账号（默认值仅本地开发） |
| `BEAR_REDIS_HOST` / `BEAR_REDIS_PORT` | `127.0.0.1` / `6379` | Redis 地址 |
| `BEAR_REDIS_USERNAME` / `BEAR_REDIS_PASSWORD` | 空 | Redis 账号（可空） |
| `BEAR_REDIS_DATABASE` | `0` | Redis 库号 |
| `BEAR_ADMIN_JWT_SECRET` | 开发占位值 | 管理端 JWT 签名密钥，生产必须注入强随机值 |
| `BEAR_ADMIN_DATA_SECRET` | 空（回落 JWT 密钥） | Token 明文 / 数据源密码 AES 加密密钥 |
| `BEAR_TOS_ENDPOINT` / `BEAR_TOS_REGION` / `BEAR_TOS_BUCKET` | 火山 TOS 默认值 | 对象存储配置 |
| `BEAR_TOS_ACCESS_KEY` / `BEAR_TOS_SECRET_KEY` | 空 | 对象存储访问密钥 |

## MCP 接入

Streamable HTTP 协议，接入流程：

1. `POST /mcp` 携带 `Authorization: Bearer <token>` 发起 `initialize`；
2. 保存响应头 `Mcp-Session-Id` 并在后续请求中携带；
3. 通过 `tools/list` 查看当前 Token 有权调用的工具（内置工具 + 动态工具，按 Token 选择过滤）；
4. 通过 `tools/call` 调用工具，动态工具由网关拦截并路由到 Groovy 沙箱执行。

## 安全设计

- **双重鉴权**：MCP 网关 Bearer Token + ApiKey 校验；管理端独立 JWT 会话。
- **Groovy 沙箱**：脚本编译期白名单与运行时安全定制器双重拦截，阻断反射逃逸与敏感类访问。
- **敏感值脱敏**：动态脚本响应与审计日志中的敏感字段统一脱敏。
- **Redis 权限策略**：脚本仅可访问声明白名单内的 key、命令与 Hash 字段，杜绝越权访问。
- **数据加密**：Token 明文与数据源密码使用独立密钥 AES 加密存储。
- **全链路审计**：MCP 调用、管理端操作、动态工具执行均落审计日志。

## License

未指定（私有部署 / 内部项目）。
