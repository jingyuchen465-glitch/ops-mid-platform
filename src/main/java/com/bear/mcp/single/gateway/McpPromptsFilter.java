package com.bear.mcp.single.gateway;

import com.bear.mcp.single.core.context.McpUserContext;
import com.bear.mcp.single.core.context.McpUserContextHolder;
import com.bear.mcp.single.core.entity.McpPromptTemplateEntity;
import com.bear.mcp.single.core.prompt.PromptAccessService;
import com.bear.mcp.single.core.prompt.PromptTemplateService;
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
import java.util.List;
import java.util.Map;

/**
 * 数据库动态 Prompt 的 MCP 协议过滤器。
 *
 * <p>Prompt 模板由创作空间写入数据库，不能通过静态注解注册。
 * 因此这里拦截 prompts/list 和 prompts/get，直接按 MCP JSON-RPC 结构返回。</p>
 */
@Component
@Order(22)
public class McpPromptsFilter extends OncePerRequestFilter {
    private static final String MCP_ENDPOINT = "/mcp";
    private static final String PROMPTS_LIST = "prompts/list";
    private static final String PROMPTS_GET = "prompts/get";

    private final PromptTemplateService promptTemplateService;
    private final PromptAccessService promptAccessService;
    private final ObjectMapper objectMapper;

    public McpPromptsFilter(PromptTemplateService promptTemplateService,
                            PromptAccessService promptAccessService,
                            ObjectMapper objectMapper) {
        this.promptTemplateService = promptTemplateService;
        this.promptAccessService = promptAccessService;
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
        if (!PROMPTS_LIST.equals(method) && !PROMPTS_GET.equals(method)) {
            filterChain.doFilter(cachedRequest, response);
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        if (PROMPTS_LIST.equals(method)) {
            response.getWriter().write(buildListResponse(requestIdOf(body)));
            return;
        }

        response.getWriter().write(buildGetResponse(requestIdOf(body), body));
    }

    private String buildListResponse(JsonNode requestId) throws IOException {
        ObjectNode response = baseResponse(requestId);
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode prompts = objectMapper.createArrayNode();
        McpUserContext context = McpUserContextHolder.get();
        for (McpPromptTemplateEntity entity : promptAccessService.listAccessiblePrompts(context)) {
            ObjectNode prompt = objectMapper.createObjectNode();
            prompt.put("name", entity.getPromptName());
            prompt.put("description", entity.getDescription() == null || entity.getDescription().isBlank()
                    ? entity.getTitle() : entity.getDescription());
            ArrayNode arguments = objectMapper.createArrayNode();
            for (Map<String, Object> item : promptTemplateService.parseArgumentsSchema(entity.getArgumentsSchema())) {
                ObjectNode argument = objectMapper.createObjectNode();
                argument.put("name", String.valueOf(item.getOrDefault("name", "")));
                argument.put("description", String.valueOf(item.getOrDefault("description", "")));
                argument.put("required", Boolean.TRUE.equals(item.get("required")));
                arguments.add(argument);
            }
            prompt.set("arguments", arguments);
            ObjectNode meta = objectMapper.createObjectNode();
            try {
                meta.set("linked_tools", objectMapper.readTree(entity.getLinkedToolNames()));
            } catch (Exception e) {
                meta.set("linked_tools", objectMapper.createArrayNode());
            }
            meta.put("title", entity.getTitle());
            prompt.set("_meta", meta);
            prompts.add(prompt);
        }
        result.set("prompts", prompts);
        response.set("result", result);
        return objectMapper.writeValueAsString(response);
    }

    @SuppressWarnings("unchecked")
    private String buildGetResponse(JsonNode requestId, String body) throws IOException {
        String name = promptNameOf(body);
        McpPromptTemplateEntity entity = promptTemplateService.findEnabledByName(name);
        if (entity == null) {
            return buildErrorResponse(requestId, -32602, "Prompt 不存在或未发布: " + name);
        }
        if (!promptAccessService.canAccess(McpUserContextHolder.get(), name)) {
            return buildErrorResponse(requestId, -32000, "无权限使用 Prompt: " + name);
        }

        Map<String, Object> arguments;
        try {
            JsonNode node = objectMapper.readTree(body).path("params").path("arguments");
            arguments = node.isMissingNode() || node.isNull() ? Map.of() : objectMapper.convertValue(node, Map.class);
        } catch (Exception e) {
            arguments = Map.of();
        }

        String rendered = promptTemplateService.render(entity.getTemplateContent(), arguments);
        ObjectNode response = baseResponse(requestId);
        ObjectNode result = objectMapper.createObjectNode();
        result.put("description", entity.getDescription());
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        ObjectNode content = objectMapper.createObjectNode();
        content.put("type", "text");
        content.put("text", rendered);
        message.set("content", content);
        messages.add(message);
        result.set("messages", messages);
        response.set("result", result);
        return objectMapper.writeValueAsString(response);
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

    private String promptNameOf(String body) {
        try {
            return objectMapper.readTree(body).path("params").path("name").asText(null);
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
                @Override
                public boolean isFinished() {
                    return inputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                }

                @Override
                public int read() {
                    return inputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
