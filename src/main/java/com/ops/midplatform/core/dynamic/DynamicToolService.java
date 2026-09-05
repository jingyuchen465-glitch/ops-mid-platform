package com.ops.midplatform.core.dynamic;

import com.ops.midplatform.core.audit.AuditLogService;
import com.ops.midplatform.core.context.McpUserContext;
import com.ops.midplatform.core.context.McpUserContextHolder;
import com.ops.midplatform.core.entity.McpDynamicToolEntity;
import com.ops.midplatform.core.groovy.GroovyScriptEngine;
import com.ops.midplatform.core.groovy.ScriptContext;
import com.ops.midplatform.core.groovy.ScriptResult;
import com.ops.midplatform.core.mapper.McpDynamicToolMapper;
import com.ops.midplatform.core.redis.RedisPermissionPolicy;
import com.ops.midplatform.core.selection.ToolSelectionService;
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

    /**
     * 内置工具来自 Spring AI @Tool / ToolCallbackProvider。
     */
    public static final String TYPE_BUILTIN = "BUILTIN";

    /**
     * 动态工具来自 mcp_dynamic_tool 表中的 Groovy 脚本配置。
     */
    public static final String TYPE_DYNAMIC = "DYNAMIC";

    /**
     * mcp_dynamic_tool：查询已启用的动态工具定义。
     */
    private final McpDynamicToolMapper dynamicToolMapper;

    /**
     * Token 工具选择服务。
     * tools/list 和 tools/call 都要检查当前 Token 是否选择了对应工具。
     */
    private final ToolSelectionService toolSelectionService;

    /**
     * 真正执行 Groovy 脚本的引擎。
     */
    private final GroovyScriptEngine groovyScriptEngine;

    /**
     * 动态工具调用不管成功失败都要落审计日志。
     */
    private final AuditLogService auditLogService;

    /**
     * 用于解析 linked_request_keys，以及记录审计参数摘要。
     */
    private final ObjectMapper objectMapper;

    private final RedisPermissionPolicy redisPermissionPolicy;

    public DynamicToolService(McpDynamicToolMapper dynamicToolMapper,
                              ToolSelectionService toolSelectionService,
                              GroovyScriptEngine groovyScriptEngine,
                              AuditLogService auditLogService,
                              ObjectMapper objectMapper,
                              RedisPermissionPolicy redisPermissionPolicy) {
        this.dynamicToolMapper = dynamicToolMapper;
        this.toolSelectionService = toolSelectionService;
        this.groovyScriptEngine = groovyScriptEngine;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
        this.redisPermissionPolicy = redisPermissionPolicy;
    }

    public Optional<DynamicTool> findEnabledByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        McpDynamicToolEntity entity = dynamicToolMapper.findEnabledByName(name);
        return entity != null ? Optional.of(toDynamicTool(entity)) : Optional.empty();
    }

    /**
     * 计算当前用户最终可以看到的工具列表。
     *
     * 这里对应 MCP 的 tools/list：
     * 1. 先看当前 Token 选择了哪些工具。
     * 2. 再看用户角色是否具备这些工具资格。
     * 3. 动态工具需要从数据库读取描述和 inputSchema；内置工具只返回名称占位。
     */
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

    /**
     * 执行动态工具。
     *
     * 这里对应 MCP 的 tools/call：
     * 1. 工具必须存在且启用。
     * 2. 当前用户角色必须有资格。
     * 3. 当前 Token 必须选择了这个工具。
     * 4. 通过 GroovyScriptEngine 执行脚本。
     * 5. finally 中记录审计日志。
     */
    public ScriptResult execute(String toolName, Map<String, Object> params) {
        long startedAt = System.currentTimeMillis();
        McpUserContext context = McpUserContextHolder.get();
        Long userId = context != null ? context.userId() : null;
        String userName = context != null ? context.userName() : null;
        String status = "SUCCESS";
        String error = null;
        String responseSummary = null;
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
                    tool.linkedDataSourceIds(),
                    tool.linkedRedisPermissions(),
                    30000
            ));
            if (!result.success()) {
                status = "ERROR";
                error = result.errorMessage();
            }
            responseSummary = toResponseSummary(result);
            return result;
        } catch (Exception e) {
            status = "ERROR";
            error = e.getMessage();
            result = ScriptResult.failure(error, System.currentTimeMillis() - startedAt);
            responseSummary = toResponseSummary(result);
            return result;
        } finally {
            auditLogService.recordToolCall(
                    userId,
                    userName,
                    toolName,
                    status,
                    System.currentTimeMillis() - startedAt,
                    toJson(params),
                    responseSummary,
                    error
            );
        }
    }

    /**
     * 审计响应摘要优先记录脚本真正 return 的结果。
     *
     * <p>失败时记录一个结构化摘要，方便管理后台看出是脚本失败还是网关拦截失败。</p>
     */
    private String toResponseSummary(ScriptResult result) {
        if (result == null) {
            return null;
        }
        if (result.success()) {
            return toJson(result.result());
        }
        return toJson(Map.of(
                "success", false,
                "errorMessage", result.errorMessage() == null ? "" : result.errorMessage()
        ));
    }

    /**
     * 审计日志里记录的是参数摘要，不要求反序列化回完整业务对象。
     */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    /**
     * Entity 是数据库表对象，DynamicTool 是业务执行对象。
     * Service 内部完成转换，避免数据库字段直接扩散到运行链路。
     */
    private DynamicTool toDynamicTool(McpDynamicToolEntity entity) {
        return new DynamicTool(
                entity.getToolName(),
                entity.getToolDescription(),
                entity.getInputSchema(),
                entity.getGroovyScript(),
                parseStringList(entity.getLinkedRequestKeys()),
                parseLongList(entity.getLinkedDataSourceIds()),
                redisPermissionPolicy.parse(entity.getLinkedRedisPermissions()),
                Integer.valueOf(1).equals(entity.getEnabled())
        );
    }

    /**
     * linked_request_keys 在数据库中是 JSON 字符串，这里转换成脚本执行时使用的白名单列表。
     */
    private List<String> parseStringList(String json) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * linked_data_source_ids 在数据库中是 JSON 字符串，这里转换成脚本执行时使用的数据源白名单。
     */
    private List<Long> parseLongList(String json) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
