package com.bear.mcp.single.gateway;

import com.bear.mcp.single.core.audit.AuditLogService;
import com.bear.mcp.single.core.context.McpUserContext;
import com.bear.mcp.single.core.context.McpUserContextHolder;
import com.bear.mcp.single.core.entity.McpResourceEntity;
import com.bear.mcp.single.core.resource.ResourceAccessService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** 数据库 + TOS 动态 Resource 的 MCP 协议过滤器。 */
@Component
@Order(18)
public class McpResourcesFilter extends OncePerRequestFilter {
    private static final String MCP_ENDPOINT = "/mcp";
    private static final String RESOURCES_LIST = "resources/list";
    private static final String RESOURCES_READ = "resources/read";

    private final ResourceAccessService resourceAccessService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public McpResourcesFilter(ResourceAccessService resourceAccessService,
                              AuditLogService auditLogService,
                              ObjectMapper objectMapper) {
        this.resourceAccessService = resourceAccessService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isMcpPost(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        CachedRequest cachedRequest = new CachedRequest(request);
        String body = cachedRequest.body();
        String method = methodOf(body);
        if (!RESOURCES_LIST.equals(method) && !RESOURCES_READ.equals(method)) {
            filterChain.doFilter(cachedRequest, response);
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        if (RESOURCES_LIST.equals(method)) {
            response.getWriter().write(buildListResponse(requestIdOf(body)));
            return;
        }
        response.getWriter().write(buildReadResponse(requestIdOf(body), body));
    }

    private String buildListResponse(JsonNode requestId) throws IOException {
        long startedAt = System.currentTimeMillis();
        ObjectNode response = baseResponse(requestId);
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode resources = objectMapper.createArrayNode();
        McpUserContext context = McpUserContextHolder.get();
        for (McpResourceEntity entity : resourceAccessService.listAccessibleResources(context)) {
            ObjectNode resource = objectMapper.createObjectNode();
            resource.put("uri", entity.getResourceUri());
            resource.put("name", entity.getName());
            resource.put("description", entity.getDescription());
            resource.put("mimeType", entity.getMimeType() == null ? "text/markdown" : entity.getMimeType());
            resources.add(resource);
        }
        result.set("resources", resources);
        response.set("result", result);
        String responseBody = objectMapper.writeValueAsString(response);
        recordResourceAudit(context, "list", "SUCCESS", startedAt,
                Map.of("method", RESOURCES_LIST, "count", resources.size()), responseBody, null);
        return responseBody;
    }

    private String buildReadResponse(JsonNode requestId, String body) throws IOException {
        long startedAt = System.currentTimeMillis();
        McpUserContext context = McpUserContextHolder.get();
        String uri = resourceUriOf(body);
        McpResourceEntity entity = resourceAccessService.findAccessibleByUri(context, uri);
        if (entity == null) {
            String message = "Resource 不存在、未发布或无权限读取: " + uri;
            String errorResponse = buildErrorResponse(requestId, -32000, message);
            recordResourceAudit(context, uri, "ERROR", startedAt, null, message);
            return errorResponse;
        }

        ObjectNode response = baseResponse(requestId);
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode content = objectMapper.createObjectNode();
        content.put("uri", entity.getResourceUri());
        content.put("mimeType", entity.getMimeType() == null ? "text/markdown" : entity.getMimeType());
        content.put("text", resourceAccessService.readText(entity));
        contents.add(content);
        result.set("contents", contents);
        response.set("result", result);
        String responseBody = objectMapper.writeValueAsString(response);
        recordResourceAudit(context, uri, "SUCCESS", startedAt, responseBody, null);
        return responseBody;
    }

    private void recordResourceAudit(McpUserContext context, String uri, String status, long startedAt,
                                     String responseSummary, String errorMessage) {
        recordResourceAudit(context, uri, status, startedAt,
                Map.of("method", RESOURCES_READ, "uri", uri == null ? "" : uri), responseSummary, errorMessage);
    }

    private void recordResourceAudit(McpUserContext context, String uri, String status, long startedAt,
                                     Map<String, Object> requestSummary, String responseSummary, String errorMessage) {
        auditLogService.recordToolCall(
                context != null ? context.userId() : null,
                context != null ? context.userName() : null,
                "RESOURCE:" + (uri == null || uri.isBlank() ? "<unknown>" : uri),
                status,
                System.currentTimeMillis() - startedAt,
                toJson(requestSummary),
                responseSummary,
                errorMessage
        );
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private ObjectNode baseResponse(JsonNode requestId) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        if (requestId != null && !requestId.isMissingNode()) {
            response.set("id", requestId);
        }
        return response;
    }

    private String buildErrorResponse(JsonNode requestId, int code, String message) throws IOException {
        ObjectNode response = baseResponse(requestId);
        ObjectNode error = objectMapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        response.set("error", error);
        return objectMapper.writeValueAsString(response);
    }

    private String resourceUriOf(String body) {
        try {
            return objectMapper.readTree(body).path("params").path("uri").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    private JsonNode requestIdOf(String body) {
        try {
            return objectMapper.readTree(body).path("id");
        } catch (Exception e) {
            return null;
        }
    }

    private String methodOf(String body) {
        try {
            return objectMapper.readTree(body).path("method").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isMcpPost(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && MCP_ENDPOINT.equals(pathOf(request));
    }

    private String pathOf(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        return contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)
                ? path.substring(contextPath.length()) : path;
    }

    private static class CachedRequest extends HttpServletRequestWrapper {
        private final byte[] body;

        CachedRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.body = request.getInputStream().readAllBytes();
        }

        String body() {
            return new String(body, StandardCharsets.UTF_8);
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override public boolean isFinished() { return inputStream.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener listener) { }
                @Override public int read() { return inputStream.read(); }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
