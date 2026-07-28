package com.bear.mcp.single.admin.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 管理端保存请求配置的请求参数。 */
@Data
public class AdminRequestConfigSaveReq {
    /** 接口唯一 ID，例如 API0000000001。 */
    @NotBlank(message = "接口ID不能为空")
    private String requestId;

    /** 上架后供动态工具引用的配置 key。 */
    @NotBlank(message = "配置Key不能为空")
    private String configKey;

    /** 配置名称。 */
    @NotBlank(message = "配置名称不能为空")
    private String name;

    /** 协议类型：HTTP、SOA、HESSIAN、MOCK。 */
    @NotBlank(message = "协议类型不能为空")
    private String type;

    /** HTTP 方法。 */
    private String method;

    /** HTTP 请求 URL。 */
    private String url;

    /** HTTP 请求头 JSON。 */
    private String headers;

    /** HTTP 请求体模板。 */
    private String bodyTemplate;

    /** 调试及运行时默认参数 JSON。 */
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
}
