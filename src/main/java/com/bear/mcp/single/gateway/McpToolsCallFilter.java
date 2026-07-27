package com.bear.mcp.single.gateway;

import com.bear.mcp.single.dynamic.DynamicToolService;
import com.bear.mcp.single.groovy.ScriptResult;
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

/**
 * 动态工具调用过滤器。
 *
 * <p>Spring AI 只认识通过 {@code @Tool} 注册的内置工具。我们自己的动态工具存在数据库/内存配置里，
 * 不会天然出现在 Spring AI 的 ToolCallback 里。所以当 MCP 客户端调用 {@code tools/call} 时，
 * 这里会先检查工具名：如果是动态工具，就短路执行 Groovy 脚本并直接返回 JSON-RPC 响应；
 * 如果不是动态工具，就继续放行给 Spring AI，让它处理内置工具。</p>
 */
@Component
@Order(21)
public class McpToolsCallFilter extends OncePerRequestFilter {

    private static final String MCP_ENDPOINT = "/mcp";
    private static final String TOOLS_CALL = "tools/call";

    private final DynamicToolService dynamicToolService;
    private final ObjectMapper objectMapper;

    public McpToolsCallFilter(DynamicToolService dynamicToolService, ObjectMapper objectMapper) {
        this.dynamicToolService = dynamicToolService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 只处理 POST /mcp。其他页面、静态资源、健康检查都不属于 MCP tools/call 链路。
        if (!isMcpPost(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Servlet 请求体默认只能读一次。这里先缓存 body，
        // 这样本过滤器读完之后，如果要放行，Spring AI 后面仍然能再次读取请求体。
        CachedRequest cachedRequest = new CachedRequest(request);
        String body = cachedRequest.body();

        // MCP 是 JSON-RPC 协议，具体操作写在 body.method 里。
        // 不是 tools/call 就不处理，例如 initialize、tools/list 都交给后续链路。
        if (!TOOLS_CALL.equals(methodOf(body))) {
            filterChain.doFilter(cachedRequest, response);
            return;
        }

        // 只有动态工具才由我们执行。内置工具 hello/current_time/calculate 等继续交给 Spring AI。
        String toolName = toolNameOf(body);
        if (toolName == null || dynamicToolService.findEnabledByName(toolName).isEmpty()) {
            filterChain.doFilter(cachedRequest, response);
            return;
        }

        // 走到这里说明命中了动态工具：不再调用 filterChain.doFilter，
        // 直接执行 Groovy 脚本，并按 MCP tools/call 规范组装 JSON-RPC 响应。
        ScriptResult result = dynamicToolService.execute(toolName, argumentsOf(body));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(buildJsonRpcResponse(requestIdOf(body), result));
    }

    /**
     * 把动态工具执行结果包装成 MCP tools/call 需要的 JSON-RPC 结构。
     *
     * <p>成功时返回 result.content，其中 content 是文本数组；失败时返回 JSON-RPC error。</p>
     */
    private String buildJsonRpcResponse(JsonNode requestId, ScriptResult result) throws IOException {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        if (requestId != null && !requestId.isMissingNode()) {
            response.set("id", requestId);
        }
        if (result.success()) {
            ObjectNode resultNode = objectMapper.createObjectNode();
            ArrayNode content = objectMapper.createArrayNode();
            ObjectNode text = objectMapper.createObjectNode();
            text.put("type", "text");
            Object value = result.result();
            text.put("text", value instanceof String ? (String) value : objectMapper.writeValueAsString(value));
            content.add(text);
            resultNode.set("content", content);
            response.set("result", resultNode);
        } else {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("code", -32000);
            error.put("message", result.errorMessage() != null ? result.errorMessage() : "Dynamic tool failed");
            response.set("error", error);
        }
        return objectMapper.writeValueAsString(response);
    }

    /**
     * 提取 tools/call 的入参：params.arguments。
     *
     * <p>如果客户端没有传 arguments，就给动态脚本一个空 Map。</p>
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> argumentsOf(String body) {
        try {
            JsonNode arguments = objectMapper.readTree(body).path("params").path("arguments");
            if (arguments.isMissingNode() || arguments.isNull()) {
                return Map.of();
            }
            return objectMapper.convertValue(arguments, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    /**
     * JSON-RPC 的 id 要原样带回响应，客户端靠它匹配请求和响应。
     */
    private JsonNode requestIdOf(String body) {
        try {
            return objectMapper.readTree(body).path("id");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 提取 JSON-RPC method，例如 initialize、tools/list、tools/call。
     */
    private String methodOf(String body) {
        try {
            return objectMapper.readTree(body).path("method").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * tools/call 的工具名在 params.name。
     */
    private String toolNameOf(String body) {
        try {
            return objectMapper.readTree(body).path("params").path("name").asText(null);
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
     * 取请求路径时去掉 contextPath，兼容应用部署在非根路径的情况。
     */
    private String pathOf(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        return contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)
                ? path.substring(contextPath.length()) : path;
    }

    /**
     * 可重复读取请求体的 RequestWrapper。
     *
     * <p>Servlet InputStream 默认读完就没了。如果本过滤器读 body 判断是不是动态工具，
     * 又决定放行给 Spring AI，那么 Spring AI 还需要再读一次 body。这个 wrapper 会把 body
     * 存成 byte[]，每次 getInputStream/getReader 都从 byte[] 创建新的读取流。</p>
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
