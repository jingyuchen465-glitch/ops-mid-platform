package com.bear.mcp.single.core.entity;

import lombok.Data;

import java.util.Date;

/** 对应 mcp_request_config 表，承载 HTTP、SOA、Hessian 等企业请求配置。 */
@Data
public class McpRequestConfigEntity {
    /** 主键。 */
    private Long id;
    /** 接口唯一 ID，例如 API0000000001。 */
    private String requestId;
    /** 上架后供动态工具引用的配置 key。 */
    private String configKey;
    /** 配置名称。 */
    private String name;
    /** 协议类型：HTTP、SOA、HESSIAN、MOCK。 */
    private String type;
    /** HTTP 方法，例如 GET、POST；MOCK 类型可为 MOCK。 */
    private String method;
    /** HTTP 请求 URL。 */
    private String url;
    /** HTTP 请求头 JSON。 */
    private String headers;
    /** HTTP 请求体模板，支持 {{key}} 占位符。 */
    private String bodyTemplate;
    /** 调试及运行时的默认参数 JSON。 */
    private String paramsDefault;
    /** HTTP 连接超时，单位毫秒。 */
    private Integer connectTimeoutMs;
    /** HTTP 读取超时，单位毫秒。 */
    private Integer readTimeoutMs;
    /** SOA/Hessian 服务名称。 */
    private String serviceName;
    /** SOA/Hessian 方法名称。 */
    private String methodName;
    /** SOA/Hessian 参数 schema JSON。 */
    private String argsSchema;
    /** 创建者用户 ID。 */
    private Long creatorId;
    /** 是否启用：1-启用，0-禁用。 */
    private Integer isEnabled;
    /** 每分钟最大调用次数，0 表示不限流。 */
    private Integer rateLimitPerMinute;
    /** 发布状态：0-草稿，1-已上架不公开，2-已上架公开。 */
    private Integer publishStatus;
    /** 接口描述。 */
    private String description;
    /** 接口分类。 */
    private String category;
    /** 创建时间。 */
    private Date createTime;
    /** 更新时间。 */
    private Date updateTime;
}
