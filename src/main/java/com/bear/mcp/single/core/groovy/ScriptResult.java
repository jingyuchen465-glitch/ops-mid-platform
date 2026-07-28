package com.bear.mcp.single.core.groovy;

public record ScriptResult(
        boolean success,
        Object result,
        String errorMessage,
        long durationMs
) {
    public static ScriptResult success(Object result, long durationMs) {
        return new ScriptResult(true, result, null, durationMs);
    }

    public static ScriptResult failure(String errorMessage, long durationMs) {
        return new ScriptResult(false, null, errorMessage, durationMs);
    }
}
