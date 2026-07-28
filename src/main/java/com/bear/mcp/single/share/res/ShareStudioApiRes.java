package com.bear.mcp.single.share.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 创作空间 API 配置响应。
 *
 * <p>这个 Res 面向前端页面展示，字段来自 mcp_request_config，
 * 但不会直接把 Entity 暴露给 Controller。</p>
 */
@Data
public class ShareStudioApiRes {

    /**
     * 数据库主键。
     *
     * <p>前端编辑、调试、发布某条 API 时通过它定位记录。</p>
     */
    private Long id;

    /**
     * 接口唯一 ID。
     *
     * <p>面向页面展示和排查问题，比数据库主键更适合展示给用户。</p>
     */
    private String requestId;

    /**
     * 配置 Key。
     *
     * <p>后续动态工具脚本通过 runRequest 引用这条 API。</p>
     */
    private String configKey;

    /**
     * API 名称。
     *
     * <p>用于列表卡片、编辑页标题等用户可见位置。</p>
     */
    private String name;

    /**
     * 协议类型。
     *
     * <p>第 7 课只展示 HTTP。</p>
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
     * <p>页面会展示它，调试时也会用它发起真实请求。</p>
     */
    private String url;

    /**
     * HTTP Header JSON。
     *
     * <p>以字符串返回，前端编辑器负责展示和修改。</p>
     */
    private String headers;

    /**
     * 请求体模板。
     *
     * <p>支持 {{key}} 占位符，执行时由参数替换。</p>
     */
    private String bodyTemplate;

    /**
     * 默认参数 JSON。
     *
     * <p>保存后的默认参数，和“本次调试参数”不是一回事。</p>
     */
    private String paramsDefault;

    /**
     * 连接超时，单位毫秒。
     */
    private Integer connectTimeoutMs;

    /**
     * 读取超时，单位毫秒。
     */
    private Integer readTimeoutMs;

    /**
     * 创建者用户 ID。
     *
     * <p>创作空间用它判断“我的 API”。</p>
     */
    private Long creatorId;

    /**
     * 是否启用。
     *
     * <p>运行时开关，1 表示允许调用，0 表示禁用。</p>
     */
    private Integer isEnabled;

    /**
     * 每分钟限流。
     *
     * <p>0 表示不限流。</p>
     */
    private Integer rateLimitPerMinute;

    /**
     * 发布状态。
     *
     * <p>0 表示草稿/下线；1 表示已上线但不公开；
     * 2 表示已上线且公开到社区。</p>
     */
    private Integer publishStatus;

    /**
     * API 描述。
     *
     * <p>用于列表摘要和社区展示。</p>
     */
    private String description;

    /**
     * API 分类。
     *
     * <p>用于创作空间和社区的归类展示。</p>
     */
    private String category;

    /**
     * 创建时间。
     *
     * <p>统一返回东八区格式，避免前端显示少 8 小时。</p>
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间。
     *
     * <p>统一返回东八区格式，避免前端自行处理时区。</p>
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
