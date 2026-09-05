package com.ops.midplatform.admin.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/** 管理端请求配置返回对象。 */
@Data
public class AdminRequestConfigRes {
    /** 主键。 */
    private Long id;

    /** 接口唯一 ID。 */
    private String requestId;

    /** 上架后供动态工具引用的配置 key。 */
    private String configKey;

    /** 配置名称。 */
    private String name;

    /** 协议类型。 */
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

    /** 每分钟最大调用次数。 */
    private Integer rateLimitPerMinute;

    /** 发布状态。 */
    private Integer publishStatus;

    /** 接口描述。 */
    private String description;

    /** 接口分类。 */
    private String category;

    /** 创建时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /** 更新时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
