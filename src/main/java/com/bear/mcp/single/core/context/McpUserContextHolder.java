package com.bear.mcp.single.core.context;

public final class McpUserContextHolder {

    private static final ThreadLocal<McpUserContext> HOLDER = new ThreadLocal<>();

    private McpUserContextHolder() {
    }

    public static void set(McpUserContext context) {
        HOLDER.set(context);
    }

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

    public static void clear() {
        HOLDER.remove();
    }
}
