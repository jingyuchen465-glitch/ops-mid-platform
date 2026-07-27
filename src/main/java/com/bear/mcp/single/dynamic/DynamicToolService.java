package com.bear.mcp.single.dynamic;

import com.bear.mcp.single.audit.AuditLogService;
import com.bear.mcp.single.context.McpUserContext;
import com.bear.mcp.single.context.McpUserContextHolder;
import com.bear.mcp.single.groovy.GroovyScriptEngine;
import com.bear.mcp.single.groovy.ScriptContext;
import com.bear.mcp.single.groovy.ScriptResult;
import com.bear.mcp.single.selection.ToolSelectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DynamicToolService {

    public static final String TYPE_BUILTIN = "BUILTIN";
    public static final String TYPE_DYNAMIC = "DYNAMIC";

    private final Map<String, DynamicTool> tools = new ConcurrentHashMap<>();
    private final ToolSelectionService toolSelectionService;
    private final GroovyScriptEngine groovyScriptEngine;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public DynamicToolService(ToolSelectionService toolSelectionService,
                              GroovyScriptEngine groovyScriptEngine,
                              AuditLogService auditLogService,
                              ObjectMapper objectMapper) {
        this.toolSelectionService = toolSelectionService;
        this.groovyScriptEngine = groovyScriptEngine;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
        seed();
    }

    public Optional<DynamicTool> findEnabledByName(String name) {
        DynamicTool tool = tools.get(name);
        return tool != null && tool.enabled() ? Optional.of(tool) : Optional.empty();
    }

    public List<ToolInfo> getUserFinalTools(Long userId, Long tokenId, Set<String> allowedTools) {
        List<String> selected = toolSelectionService.listSelectedTools(tokenId);
        if (selected.isEmpty()) {
            return List.of();
        }
        List<ToolInfo> result = new ArrayList<>();
        for (String toolName : selected) {
            if (allowedTools == null || !allowedTools.contains(toolName)) {
                continue;
            }
            DynamicTool dynamicTool = tools.get(toolName);
            if (dynamicTool != null && dynamicTool.enabled()) {
                result.add(new ToolInfo(dynamicTool.name(), dynamicTool.description(), dynamicTool.inputSchema(), TYPE_DYNAMIC));
            } else {
                result.add(new ToolInfo(toolName, "", "{}", TYPE_BUILTIN));
            }
        }
        return result;
    }

    public ScriptResult execute(String toolName, Map<String, Object> params) {
        long startedAt = System.currentTimeMillis();
        McpUserContext context = McpUserContextHolder.get();
        Long userId = context != null ? context.userId() : null;
        String userName = context != null ? context.userName() : null;
        String status = "SUCCESS";
        String error = null;
        ScriptResult result;
        try {
            DynamicTool tool = findEnabledByName(toolName)
                    .orElseThrow(() -> new IllegalArgumentException("动态工具不存在或已禁用: " + toolName));
            if (context == null || !context.allowedTools().contains(toolName)) {
                throw new IllegalArgumentException("无权限使用工具: " + toolName);
            }
            result = groovyScriptEngine.execute(tool.script(), new ScriptContext(
                    params,
                    userId,
                    userName,
                    toolName,
                    tool.linkedRequestKeys(),
                    30000
            ));
            if (!result.success()) {
                status = "ERROR";
                error = result.errorMessage();
            }
            return result;
        } catch (Exception e) {
            status = "ERROR";
            error = e.getMessage();
            result = ScriptResult.failure(error, System.currentTimeMillis() - startedAt);
            return result;
        } finally {
            auditLogService.recordToolCall(
                    userId,
                    userName,
                    toolName,
                    status,
                    System.currentTimeMillis() - startedAt,
                    toJson(params),
                    null,
                    error
            );
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private void seed() {
        tools.put("echo_dynamic", new DynamicTool(
                "echo_dynamic",
                "动态工具示例：回显输入参数，并演示通过 runRequest 调用白名单请求配置。",
                """
                        {"type":"object","properties":{"message":{"type":"string","description":"要回显的内容"}},"required":["message"]}
                        """,
                """
                        def clock = runRequest.runRequest("demo_clock", [message: params.message])
                        return [
                          message: params.message,
                          currentUser: userName,
                          clock: clock
                        ]
                        """,
                List.of("demo_clock"),
                true
        ));
    }
}
