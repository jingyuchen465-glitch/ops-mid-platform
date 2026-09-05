package com.ops.midplatform.share.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创作空间保存 HTTP API 配置的请求。
 *
 * <p>这个 Req 对应前端 API 编辑页的表单，不直接使用 Entity。
 * 这样可以把“页面允许用户填写什么”和“数据库实际保存什么”分开。</p>
 */
@Data
public class ShareStudioApiSaveReq {

    /**
     * 接口唯一 ID。
     *
     * <p>主要用于页面展示和后续追踪，例如 API0000000001。</p>
     */
    @NotBlank(message = "接口ID不能为空")
    private String requestId;

    /**
     * 配置 Key。
     *
     * <p>后续动态工具脚本通过 runRequest("configKey", params) 引用这条 API。</p>
     */
    @NotBlank(message = "配置Key不能为空")
    private String configKey;

    /**
     * API 名称。
     *
     * <p>面向人展示，用来说明这条 API 是做什么的。</p>
     */
    @NotBlank(message = "API名称不能为空")
    private String name;

    /**
     * 协议类型。
     *
     * <p>第 7 课只讲 HTTP API 接入，所以后端会统一归一化成 HTTP。</p>
     */
    private String type;

    /**
     * HTTP 方法。
     *
     * <p>例如 GET、POST、PUT、DELETE。</p>
     */
    private String method;

    /**
     * 外部 HTTP API URL。
     *
     * <p>可以包含 {{key}} 占位符，例如 /course?pageNum={{pageNum}}。</p>
     */
    @NotBlank(message = "请求URL不能为空")
    private String url;

    /**
     * HTTP Header JSON。
     *
     * <p>必须是 JSON 对象字符串，例如 {"Authorization":"Bearer xxx"}。</p>
     */
    private String headers;

    /**
     * 请求体模板。
     *
     * <p>POST/PUT 请求可以在这里配置 Body，同样支持 {{key}} 占位符。</p>
     */
    private String bodyTemplate;

    /**
     * 默认参数 JSON。
     *
     * <p>保存到配置里的参数模板，后续 runRequest 不传同名参数时可以作为默认值。</p>
     */
    private String paramsDefault;

    /**
     * 连接超时时间，单位毫秒。
     *
     * <p>连接建立超过这个时间仍未成功，就认为请求失败。</p>
     */
    private Integer connectTimeoutMs;

    /**
     * 读取超时时间，单位毫秒。
     *
     * <p>连接成功后等待响应体的最长时间。</p>
     */
    private Integer readTimeoutMs;

    /**
     * 是否启用。
     *
     * <p>这是运行时开关，1 表示允许调用，0 表示禁用。创作空间通常默认启用，
     * 管理后台可以把它作为治理开关使用。</p>
     */
    private Integer isEnabled;

    /**
     * 每分钟最大调用次数。
     *
     * <p>0 表示不限流。第 7 课先保存字段，完整限流治理后续课程再展开。</p>
     */
    private Integer rateLimitPerMinute;

    /**
     * API 描述。
     *
     * <p>用于说明接口用途，后续公开到社区时也会作为卡片说明。</p>
     */
    private String description;

    /**
     * API 分类。
     *
     * <p>例如 course、order、user，用于列表筛选或社区归类。</p>
     */
    private String category;
}
