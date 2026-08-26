package com.bear.mcp.single.core.request;

import java.util.Map;

/**
 * 运行时请求配置。
 *
 * <p>它来自 McpRequestConfigEntity，但不直接作为数据库对象向外传递。
 * 动态工具脚本通过 runRequest(key, params) 触发这里的配置执行。</p>
 */
public record RequestConfig(
        /** 企业请求配置 ID，课堂版用于展示。 */
        String requestId,
        /** 稳定配置 key，动态工具通过这个值引用请求配置。 */
        String key,
        /** 请求配置名称。 */
        String name,
        /** 请求类型，当前课堂版主要接入 HTTP。 */
        String type,
        /** HTTP method，例如 GET、POST。 */
        String method,
        /** HTTP URL，支持 {{参数名}} 占位符。 */
        String url,
        /** HTTP 请求头，值支持 {{参数名}} 占位符。 */
        Map<String, String> headers,
        /** HTTP body 模板，支持 {{参数名}} 占位符。 */
        String bodyTemplate,
        /** 默认参数，执行时会和脚本传入参数合并。 */
        Map<String, Object> paramsDefault,
        /** 连接超时时间，单位毫秒。 */
        Integer connectTimeoutMs,
        /** 读取超时时间，单位毫秒。 */
        Integer readTimeoutMs,
        /** SOA/Hessian 等协议预留的服务名。 */
        String serviceName,
        /** SOA/Hessian 等协议预留的方法名。 */
        String methodName,
        /** 参数 schema，供管理端展示或后续创作空间使用。 */
        String argsSchema,
        /** 创建人 ID。 */
        Long creatorId,
        /** 每分钟限流次数，0 或 null 表示不限流。 */
        Integer rateLimitPerMinute,
        /** 发布状态，课堂版主要用于展示。 */
        Integer publishStatus,
        /** 请求配置说明。 */
        String description,
        /** 请求配置分类。 */
        String category,
        /** 是否启用。 */
        boolean enabled
) {
}
