package com.bear.mcp.single.core.context;

import java.util.Set;

/**
 * 当前 MCP 请求的用户上下文。
 *
 * <p>ApiKeyAuthFilter 鉴权通过后创建它，后续 tools/list、tools/call、审计日志都从这里读取当前用户。</p>
 */
public record McpUserContext(
        /** 当前 Token 归属用户 ID。 */
        Long userId,
        /** 当前请求使用的 Token ID。 */
        Long tokenId,
        /** 当前 Token 归属用户名。 */
        String userName,
        /** 用户启用中的角色编码集合。 */
        Set<String> roles,
        /** 用户通过角色获得的工具资格集合。 */
        Set<String> allowedTools,
        /** 当前 MCP 会话 ID，课堂版预留。 */
        String sessionId,
        /** 客户端 IP，用于审计和排查。 */
        String clientIp
) {
}
