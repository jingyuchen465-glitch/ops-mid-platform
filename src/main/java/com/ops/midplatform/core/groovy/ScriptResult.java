package com.bear.mcp.single.core.groovy;

/**
 * Groovy 脚本执行结果。
 */
public record ScriptResult(
        /** 是否执行成功。 */
        boolean success,
        /** 脚本 return 的对象。 */
        Object result,
        /** 失败时的错误信息。 */
        String errorMessage,
        /** 脚本执行耗时，单位毫秒。 */
        long durationMs
) {
    /**
     * 创建成功结果。
     */
    public static ScriptResult success(Object result, long durationMs) {
        return new ScriptResult(true, result, null, durationMs);
    }

    /**
     * 创建失败结果。
     */
    public static ScriptResult failure(String errorMessage, long durationMs) {
        return new ScriptResult(false, null, errorMessage, durationMs);
    }
}
