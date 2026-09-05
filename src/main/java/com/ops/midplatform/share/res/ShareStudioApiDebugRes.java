package com.ops.midplatform.share.res;

import lombok.Data;

/**
 * 创作空间 API 调试响应。
 *
 * <p>调试接口不只关心成功或失败，还要把外部接口响应、错误原因和耗时返回给前端，
 * 方便作者在页面上直接判断配置是否正确。</p>
 */
@Data
public class ShareStudioApiDebugRes {

    /**
     * 调试是否成功。
     *
     * <p>true 表示外部 HTTP 请求执行成功；false 表示请求配置错误、网络错误或外部接口异常。</p>
     */
    private Boolean success;

    /**
     * 外部 HTTP API 返回结果。
     *
     * <p>一般包含 HTTP 状态码、响应体等信息，前端会格式化展示。</p>
     */
    private Object result;

    /**
     * 错误信息。
     *
     * <p>调试失败时用于展示失败原因，例如 URL 不合法、连接超时等。</p>
     */
    private String errorMessage;

    /**
     * 调试耗时，单位毫秒。
     *
     * <p>用于帮助作者判断外部接口是否太慢。</p>
     */
    private Long durationMs;
}
