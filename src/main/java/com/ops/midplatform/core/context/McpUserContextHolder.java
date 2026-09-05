package com.ops.midplatform.core.context;

/**
 * 保存当前线程内的 MCP 用户上下文。
 *
 * <p>Spring MVC 一次请求通常由一个线程处理，Filter 设置上下文，后续 Service 直接读取。
 * 请求结束后必须 clear，避免线程复用时串到下一次请求。</p>
 */
public final class McpUserContextHolder {

    /**
     * ThreadLocal 让同一个请求线程里的不同层代码都能拿到当前用户信息。
     */
    private static final ThreadLocal<McpUserContext> HOLDER = new ThreadLocal<>();

    private McpUserContextHolder() {
    }

    public static void set(McpUserContext context) {
        HOLDER.set(context);
    }

    /**
     * 获取当前请求上下文；非 MCP 请求或未鉴权时可能为 null。
     */
    public static McpUserContext get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        McpUserContext context = get();
        return context != null ? context.userId() : null;
    }

    public static Long getTokenId() {
        McpUserContext context = get();
        return context != null ? context.tokenId() : null;
    }

    /**
     * 请求结束时清理上下文。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
