package com.bear.mcp.single.gateway;

import com.bear.mcp.single.core.context.McpUserContext;
import com.bear.mcp.single.core.context.McpUserContextHolder;
import com.bear.mcp.single.core.dynamic.DynamicToolService;
import com.bear.mcp.single.core.dynamic.ToolInfo;
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
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * MCP tools/list 响应改写过滤器。
 *
 * <p>Spring AI 只能自动列出通过 {@code @Tool} 注册的内置工具；但我们的动态工具来自配置，
 * 不在 Spring AI 原始工具列表里。同时，不同 token 应该看到不同工具集合。因此这里采用
 * “先让 Spring AI 生成原始 tools/list，再按当前 token 改写响应”的方式。</p>
 *
 * <p>最终效果：客户端调用 {@code tools/list} 时，只能看到当前 token 已选择且有权限的工具，
 * 并且会额外看到动态工具。</p>
 */
@Component
@Order(20)
public class McpToolsListFilter extends OncePerRequestFilter {

    private static final String MCP_ENDPOINT = "/mcp";
    private static final String TOOLS_LIST = "tools/list";

    private final DynamicToolService dynamicToolService;
    private final ObjectMapper objectMapper;

    public McpToolsListFilter(DynamicToolService dynamicToolService, ObjectMapper objectMapper) {
        this.dynamicToolService = dynamicToolService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 只处理 POST /mcp。initialize、tools/list、tools/call 都是从这个入口进来，
        // 但当前过滤器后面只会真正改写 method=tools/list 的响应。
        if (!isMcpPost(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 请求体里有 JSON-RPC method。先缓存请求体并提前判断 method，
        // 这样 initialize、tools/call 等非 tools/list 请求不用再包 response。
        CachedRequest requestWrapper = new CachedRequest(request);
        String requestBody = requestWrapper.body();
        if (!TOOLS_LIST.equals(methodOf(requestBody))) {
            filterChain.doFilter(requestWrapper, response);
            return;
        }

        // 只有 tools/list 才需要缓存响应体，因为我们要等 Spring AI 生成原始工具列表后再改写。
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // 先把当前用户上下文取出来。它由 ApiKeyAuthFilter 放进 ThreadLocal。
        McpUserContext context = McpUserContextHolder.get();

        // 先放行给后面的过滤器和 Spring AI MCP Handler。
        // 如果这是 tools/list，Spring AI 会先生成“所有内置工具”的原始响应。
        filterChain.doFilter(requestWrapper, responseWrapper);

        try {
            if (context != null) {
                // 到这里才开始改写 tools/list 响应：
                // 1. 过滤内置工具
                // 2. 注入动态工具
                // 3. 保持 JSON-RPC/SSE 响应格式返回给客户端
                String original = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                String modified = modifyToolsList(original, context);
                responseWrapper.resetBuffer();
                responseWrapper.getOutputStream().write(modified.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
            // 如果改写失败，就保留 Spring AI 原始响应，避免因为治理层异常导致 /mcp 整体不可用。
        }

        // ContentCachingResponseWrapper 不会自动把缓存内容写回客户端，必须显式 copy。
        responseWrapper.copyBodyToResponse();
    }

    /**
     * 改写 Spring AI 生成的 tools/list 响应。
     *
     * <p>处理分两步：</p>
     * <p>第一步，从 Spring AI 原始 tools 中保留当前 token 选择的内置工具。</p>
     * <p>第二步，把当前 token 可见的动态工具转换成 MCP Tool 格式后追加进去。</p>
     */
    private String modifyToolsList(String originalResponse, McpUserContext context) throws Exception {
        // Spring AI 在 Streamable HTTP 下可能返回普通 JSON，也可能返回 SSE 风格 data: {...}。
        // 先把真正的 JSON 部分取出来。
        String json = extractJson(originalResponse);
        if (json == null || json.isBlank()) {
            return originalResponse;
        }
        JsonNode responseNode = objectMapper.readTree(json);
        JsonNode toolsNode = responseNode.path("result").path("tools");
        if (!toolsNode.isArray()) {
            return originalResponse;
        }

        // allowed 同时包含内置工具和动态工具，已经综合了 token 选择和用户权限。
        List<ToolInfo> allowed = dynamicToolService.getUserFinalTools(
                context.userId(), context.tokenId(), context.allowedTools());
        ArrayNode newTools = objectMapper.createArrayNode();

        // 内置工具由 Spring AI 生成完整 schema，这里只做“是否保留”的过滤。
        for (JsonNode originalTool : toolsNode) {
            String name = originalTool.path("name").asText();
            boolean selectedBuiltin = allowed.stream()
                    .anyMatch(t -> DynamicToolService.TYPE_BUILTIN.equals(t.type()) && name.equals(t.name()));
            if (selectedBuiltin) {
                newTools.add(originalTool);
            }
        }

        // 动态工具不在 Spring AI 原始列表中，所以这里手动组装 MCP Tool 描述。
        for (ToolInfo toolInfo : allowed) {
            if (!DynamicToolService.TYPE_DYNAMIC.equals(toolInfo.type())) {
                continue;
            }
            ObjectNode dynamicTool = objectMapper.createObjectNode();
            dynamicTool.put("name", toolInfo.name());
            dynamicTool.put("description", toolInfo.description());
            try {
                dynamicTool.set("inputSchema", objectMapper.readTree(toolInfo.inputSchema()));
            } catch (Exception e) {
                ObjectNode schema = objectMapper.createObjectNode();
                schema.put("type", "object");
                schema.set("properties", objectMapper.createObjectNode());
                dynamicTool.set("inputSchema", schema);
            }
            newTools.add(dynamicTool);
        }

        // 用过滤后的工具数组替换原来的 result.tools。
        ((ObjectNode) responseNode.path("result")).set("tools", newTools);
        String modifiedJson = objectMapper.writeValueAsString(responseNode);
        return wrapLike(originalResponse, modifiedJson);
    }

    /**
     * 读取 JSON-RPC method，比如 initialize、tools/list、tools/call。
     */
    private String methodOf(String requestBody) {
        try {
            return objectMapper.readTree(requestBody).path("method").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 当前过滤器只关心 Streamable HTTP 下的 POST /mcp。
     */
    private boolean isMcpPost(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && MCP_ENDPOINT.equals(pathOf(request));
    }

    /**
     * 获取请求路径并去掉 contextPath，兼容应用部署到非根路径。
     */
    private String pathOf(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        return contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)
                ? path.substring(contextPath.length()) : path;
    }

    /**
     * 从响应体中提取 JSON。
     *
     * <p>普通 JSON 响应直接返回；SSE 响应形如 {@code data: {...}}，需要取 data 后面的 JSON。</p>
     */
    private String extractJson(String response) {
        if (response == null) {
            return null;
        }
        String trimmed = response.trim();
        if (!trimmed.startsWith("id:") && !trimmed.startsWith("data:") && !trimmed.startsWith("event:")) {
            return response;
        }
        for (String line : response.split("\n")) {
            if (line.startsWith("data:")) {
                return line.substring(5).trim();
            }
        }
        return null;
    }

    /**
     * 改写后保持原响应格式。
     *
     * <p>如果原来是 SSE，就继续返回 {@code data: JSON\n\n}；
     * 如果原来是普通 JSON，就直接返回 JSON。</p>
     */
    private String wrapLike(String original, String json) {
        String trimmed = original != null ? original.trim() : "";
        if (trimmed.startsWith("id:") || trimmed.startsWith("data:") || trimmed.startsWith("event:")) {
            return "data: " + json + "\n\n";
        }
        return json;
    }

    /**
     * 可重复读取请求体的 RequestWrapper。
     *
     * <p>提前判断 JSON-RPC method 会读取 body；如果不是 tools/list 还要继续放行给 Spring AI。
     * 因此这里把 body 缓存在 byte[] 中，保证后续 Handler 仍然可以再次读取。</p>
     */
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
