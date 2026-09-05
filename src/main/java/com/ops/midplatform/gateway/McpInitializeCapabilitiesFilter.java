package com.ops.midplatform.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/** 给 Spring AI initialize 响应补充数据库动态 Prompt / Resource 能力声明。 */
@Component
@Order(19)
public class McpInitializeCapabilitiesFilter extends OncePerRequestFilter {
    private static final String MCP_ENDPOINT = "/mcp";
    private static final String INITIALIZE = "initialize";

    private final ObjectMapper objectMapper;

    public McpInitializeCapabilitiesFilter(ObjectMapper objectMapper) {
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
        if (!INITIALIZE.equals(methodOf(body))) {
            filterChain.doFilter(cachedRequest, response);
            return;
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(cachedRequest, responseWrapper);

        try {
            String original = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            String modified = addDynamicCapabilities(original);
            responseWrapper.resetBuffer();
            responseWrapper.getOutputStream().write(modified.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
        responseWrapper.copyBodyToResponse();
    }

    private String addDynamicCapabilities(String originalResponse) throws Exception {
        String json = extractJson(originalResponse);
        if (json == null || json.isBlank()) {
            return originalResponse;
        }
        JsonNode responseNode = objectMapper.readTree(json);
        JsonNode result = responseNode.path("result");
        if (!(result instanceof ObjectNode resultObject)) {
            return originalResponse;
        }
        ObjectNode capabilities;
        if (resultObject.path("capabilities").isObject()) {
            capabilities = (ObjectNode) resultObject.path("capabilities");
        } else {
            capabilities = objectMapper.createObjectNode();
            resultObject.set("capabilities", capabilities);
        }
        ObjectNode prompts = objectMapper.createObjectNode();
        prompts.put("listChanged", false);
        capabilities.set("prompts", prompts);
        ObjectNode resources = objectMapper.createObjectNode();
        resources.put("subscribe", false);
        resources.put("listChanged", false);
        capabilities.set("resources", resources);
        return wrapLike(originalResponse, objectMapper.writeValueAsString(responseNode));
    }

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

    private String wrapLike(String original, String json) {
        String trimmed = original != null ? original.trim() : "";
        if (trimmed.startsWith("id:") || trimmed.startsWith("data:") || trimmed.startsWith("event:")) {
            return "data: " + json + "\n\n";
        }
        return json;
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
