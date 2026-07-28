package com.bear.mcp.single.core.dynamic;

import com.bear.mcp.single.core.audit.AuditLogService;
import com.bear.mcp.single.core.context.McpUserContext;
import com.bear.mcp.single.core.context.McpUserContextHolder;
import com.bear.mcp.single.core.entity.McpDynamicToolEntity;
import com.bear.mcp.single.core.groovy.GroovyScriptEngine;
import com.bear.mcp.single.core.groovy.ScriptContext;
import com.bear.mcp.single.core.groovy.ScriptResult;
import com.bear.mcp.single.core.mapper.McpDynamicToolMapper;
import com.bear.mcp.single.core.selection.ToolSelectionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class DynamicToolService {

    public static final String TYPE_BUILTIN = "BUILTIN";
    public static final String TYPE_DYNAMIC = "DYNAMIC";

    private final McpDynamicToolMapper dynamicToolMapper;
    private final ToolSelectionService toolSelectionService;
    private final GroovyScriptEngine groovyScriptEngine;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public DynamicToolService(McpDynamicToolMapper dynamicToolMapper,
                              ToolSelectionService toolSelectionService,
                              GroovyScriptEngine groovyScriptEngine,
                              AuditLogService auditLogService,
                              ObjectMapper objectMapper) {
        this.dynamicToolMapper = dynamicToolMapper;
        this.toolSelectionService = toolSelectionService;
        this.groovyScriptEngine = groovyScriptEngine;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    public Optional<DynamicTool> findEnabledByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        McpDynamicToolEntity entity = dynamicToolMapper.findEnabledByName(name);
        return entity != null ? Optional.of(toDynamicTool(entity)) : Optional.empty();
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
            Optional<DynamicTool> dynamicTool = findEnabledByName(toolName);
            if (dynamicTool.isPresent()) {
                DynamicTool tool = dynamicTool.get();
                result.add(new ToolInfo(tool.name(), tool.description(), tool.inputSchema(), TYPE_DYNAMIC));
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
            if (!toolSelectionService.isToolSelected(context.tokenId(), toolName)) {
                throw new IllegalArgumentException("当前 Token 未选择工具: " + toolName);
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

    private DynamicTool toDynamicTool(McpDynamicToolEntity entity) {
        return new DynamicTool(
                entity.getToolName(),
                entity.getToolDescription(),
                entity.getInputSchema(),
                entity.getGroovyScript(),
                parseStringList(entity.getLinkedRequestKeys()),
                Integer.valueOf(1).equals(entity.getEnabled())
        );
    }

    private List<String> parseStringList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
