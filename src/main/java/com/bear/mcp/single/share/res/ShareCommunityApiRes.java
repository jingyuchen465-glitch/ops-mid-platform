package com.bear.mcp.single.share.res;

import lombok.Data;

/**
 * 社区公开 API 展示响应。
 */
@Data
public class ShareCommunityApiRes {

    /**
     * 数据库主键。
     */
    private Long id;

    /**
     * 接口唯一 ID。
     */
    private String requestId;

    /**
     * 脚本调用时使用的配置 Key。
     */
    private String configKey;

    /**
     * API 名称。
     */
    private String name;

    /**
     * 协议类型。
     */
    private String type;

    /**
     * HTTP 方法。
     */
    private String method;

    /**
     * API 描述。
     */
    private String description;

    /**
     * API 分类。
     */
    private String category;

    /**
     * 发布状态：社区只返回 2。
     */
    private Integer publishStatus;
}
