package com.bear.mcp.single.gateway;

import com.bear.mcp.single.core.auth.TokenAuthInfo;
import com.bear.mcp.single.core.auth.TokenService;
import com.bear.mcp.single.core.context.McpUserContext;
import com.bear.mcp.single.core.context.McpUserContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.UUID;

/**
 * MCP Token 鉴权过滤器。
 *
 * <p>Spring AI 自动注册了 /mcp 协议端点，这个端点不是我们自己写的 Controller。
 * 所以这里用 Servlet Filter 在 Spring AI 处理请求之前先做鉴权，并把当前用户信息放进
 * {@link McpUserContextHolder}。后面的 tools/list 过滤、动态 tools/call 执行、内置工具方法
 * 都可以从 ThreadLocal 中拿到当前用户。</p>
 */
@Component
@Order(10)
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    private static final String LOG_PREFIX = "ApiKeyAuthFilter";
    private static final String MCP_ENDPOINT = "/mcp";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String MCP_SESSION_ID = "Mcp-Session-Id";
    private static final int MAX_REQUEST_BODY_LOG_LENGTH = 20_000;

    private final TokenService tokenService;

    public ApiKeyAuthFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        boolean mcpRequest = isMcpRequest(request);
        ContentCachingRequestWrapper requestWrapper = mcpRequest
                ? new ContentCachingRequestWrapper(request, MAX_REQUEST_BODY_LOG_LENGTH)
                : null;
        HttpServletRequest currentRequest = requestWrapper != null ? requestWrapper : request;

        try {
            // 只保护 MCP 协议入口。后面补 /admin、/share 页面时，它们会有自己的登录态校验。
            if (mcpRequest) {
                String token = extractToken(currentRequest);
                TokenAuthInfo tokenAuthInfo = tokenService.validate(token).orElse(null);
                if (tokenAuthInfo == null) {
                    // MCP 客户端没有带 token，或者 token 不存在时，直接拒绝，不进入 Spring AI MCP Handler。
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Invalid MCP token\"}");
                    return;
                }

                // 复用 Spring AI MCP Streamable HTTP 协议会话 ID，用于审计和请求关联。
                String sessionId = currentRequest.getHeader(MCP_SESSION_ID);
                if (sessionId == null || sessionId.isBlank()) {
                    sessionId = UUID.randomUUID().toString();
                }

                // 把 token 对应的用户、角色、可用工具等信息放入当前请求线程。
                // 后续 Filter 和 @Tool 方法不需要反复查 token，直接从 McpUserContextHolder 读取。
                McpUserContextHolder.set(new McpUserContext(
                        tokenAuthInfo.userId(),
                        tokenAuthInfo.tokenId(),
                        tokenAuthInfo.userName(),
                        tokenAuthInfo.roleCodes(),
                        tokenAuthInfo.allowedTools(),
                        tokenAuthInfo.allowedPrompts(),
                        tokenAuthInfo.allowedResources(),
                        sessionId,
                        clientIp(currentRequest)
                ));
            }
            filterChain.doFilter(currentRequest, response);
        } finally {
            logRequestBody(requestWrapper);
            // Tomcat 工作线程会复用。请求结束必须清理 ThreadLocal，
            // 避免下一个请求误读到上一个用户的身份信息。
            McpUserContextHolder.clear();
        }
    }

    /**
     * 判断当前请求是不是 MCP 协议入口。
     *
     * <p>这里去掉 contextPath，是为了兼容应用将来部署在 /xxx 这种上下文路径下。</p>
     */
    private boolean isMcpRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return MCP_ENDPOINT.equals(path);
    }

    /**
     * MCP 客户端推荐使用 Authorization: Bearer mcp_xxx。
     *
     * <p>保留 mcp_token 查询参数，是为了兼容某些不方便传 header 的调试或 SSE 场景。</p>
     */
    private String extractToken(HttpServletRequest request) {
        logRequestHeaders(request);

        String header = request.getHeader(AUTHORIZATION);
        if (header != null && header.startsWith(BEARER)) {
            return header.substring(BEARER.length()).trim();
        }
        String queryToken = request.getParameter("mcp_token");
        return queryToken != null ? queryToken.trim() : null;
    }

    /**
     * 调试客户端接入问题时，打印当前 MCP 请求携带的所有请求头。
     */
    private void logRequestHeaders(HttpServletRequest request) {
        if (!log.isInfoEnabled()) {
            return;
        }

        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (!headers.isEmpty()) {
                headers.append(", ");
            }
            headers.append(name).append("=").append(maskHeaderValue(name, request.getHeaders(name)));
        }
        log.info("{}_extractToken requestHeaders=[{}]", LOG_PREFIX, headers);
    }

    /**
     * 在下游处理完成后打印请求体，避免提前读取 body 导致 Spring AI MCP Handler 读不到内容。
     */
    private void logRequestBody(ContentCachingRequestWrapper requestWrapper) {
        if (requestWrapper == null || !log.isInfoEnabled()) {
            return;
        }

        byte[] content = requestWrapper.getContentAsByteArray();
        if (content.length == 0) {
            log.info("{}_doFilterInternal requestBody=<empty>", LOG_PREFIX);
            return;
        }

        String body = new String(content, StandardCharsets.UTF_8);
        boolean truncated = content.length >= MAX_REQUEST_BODY_LOG_LENGTH;
        log.info("{}_doFilterInternal requestBody={}{}", LOG_PREFIX, body, truncated ? "...<truncated>" : "");
    }

    private String maskHeaderValue(String name, Enumeration<String> values) {
        StringBuilder result = new StringBuilder();
        while (values != null && values.hasMoreElements()) {
            if (!result.isEmpty()) {
                result.append("; ");
            }
            String value = values.nextElement();
            result.append(isSensitiveHeader(name) ? mask(value) : value);
        }
        return result.toString();
    }

    private boolean isSensitiveHeader(String name) {
        return AUTHORIZATION.equalsIgnoreCase(name)
                || "Cookie".equalsIgnoreCase(name)
                || "Set-Cookie".equalsIgnoreCase(name);
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int visibleChars = Math.min(8, value.length());
        return value.substring(0, visibleChars) + "***";
    }

    /**
     * 获取调用方 IP。生产环境如果经过 Nginx/网关，一般会优先看 X-Forwarded-For 或 X-Real-IP。
     */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp != null && !realIp.isBlank() ? realIp : request.getRemoteAddr();
    }
}
