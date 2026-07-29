package com.bear.mcp.single.core.tools;

import com.bear.mcp.single.core.context.McpUserContextHolder;
import com.bear.mcp.single.core.entity.McpDynamicToolEntity;
import com.bear.mcp.single.core.entity.McpRequestConfigEntity;
import com.bear.mcp.single.core.mapper.McpDynamicToolMapper;
import com.bear.mcp.single.core.mapper.McpRequestConfigMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通过 MCP Tool 创建和维护动态 Tool 草稿。
 *
 * <p>它把企业版“Agent 自己创建 Tool”的思路收敛到课堂版：
 * Agent 先创建 API 配置，再创建 Groovy 动态 Tool，并用白名单约束脚本能调用哪些 API。</p>
 */
@Component
public class DynamicStudioTools {

    private static final int STATUS_DRAFT = 0;

    private final McpDynamicToolMapper dynamicToolMapper;
    private final McpRequestConfigMapper requestConfigMapper;
    private final ObjectMapper objectMapper;

    public DynamicStudioTools(McpDynamicToolMapper dynamicToolMapper,
                              McpRequestConfigMapper requestConfigMapper,
                              ObjectMapper objectMapper) {
        this.dynamicToolMapper = dynamicToolMapper;
        this.requestConfigMapper = requestConfigMapper;
        this.objectMapper = objectMapper;
    }

    @Tool(name = "create_dynamic_tool",
            description = "创建动态 MCP Tool 草稿，把一个或多个已接入的 API 能力包装成 AI Agent 可见的工具。创建后默认是草稿，需要发布后才会进入 tools/list 和 tools/call。")
    public Map<String, Object> createDynamicTool(
            @ToolParam(description = "MCP Tool 名称，全局唯一，建议小写字母、数字、下划线", required = true)
            String tool_name,
            @ToolParam(description = "工具描述，会影响 Agent 选择工具", required = false)
            String tool_description,
            @ToolParam(description = "MCP inputSchema JSON 对象，定义 tools/call 可传入的参数结构", required = false)
            String input_schema,
            @ToolParam(description = "Groovy 脚本，可用 params、userId、userName、toolName、runRequest 编排业务逻辑", required = false)
            String groovy_script,
            @ToolParam(description = "API 白名单 JSON 数组，如 [\"query_course_list\"]。脚本只能通过 runRequest 调用这些 config_key", required = false)
            String linked_request_keys
    ) {
        if (McpUserContextHolder.getUserId() == null) {
            return failure("请先使用 MCP Token 调用");
        }

        try {
            validateRequired(tool_name, "tool_name 不能为空");
            String toolName = tool_name.trim();
            if (dynamicToolMapper.findByName(toolName) != null) {
                return failure("工具名已存在: " + toolName);
            }

            List<String> requestKeys = parseRequestKeys(linked_request_keys);
            validateRequestKeys(requestKeys);

            McpDynamicToolEntity entity = new McpDynamicToolEntity();
            entity.setToolName(toolName);
            entity.setToolDescription(firstNotBlank(tool_description, "通过 Groovy 脚本编排 API 请求配置。"));
            entity.setInputSchema(firstNotBlank(input_schema, defaultInputSchema()));
            entity.setGroovyScript(firstNotBlank(groovy_script, defaultScript(requestKeys)));
            entity.setLinkedRequestKeys(toJson(requestKeys));
            entity.setEnabled(0);
            entity.setPublishStatus(STATUS_DRAFT);

            validateJsonObject(entity.getInputSchema(), "input_schema 必须是 JSON 对象");
            dynamicToolMapper.insert(entity);

            Map<String, Object> result = success("动态 Tool 草稿创建成功，请发布后再通过 MCP 调用");
            result.put("id", entity.getId());
            result.put("tool_name", entity.getToolName());
            result.put("publish_status", entity.getPublishStatus());
            result.put("linked_request_keys", requestKeys);
            return result;
        } catch (Exception e) {
            return failure(e.getMessage());
        }
    }

    @Tool(name = "list_dynamic_tools",
            description = "查询当前动态 MCP Tool 草稿和已发布工具，用于确认已有工具、发布状态、启用状态以及绑定的 API 白名单。")
    public Map<String, Object> listDynamicTools(
            @ToolParam(description = "关键词，可选", required = false)
            String keyword
    ) {
        if (McpUserContextHolder.getUserId() == null) {
            return failure("请先使用 MCP Token 调用");
        }

        String normalizedKeyword = keyword != null ? keyword.trim().toLowerCase() : "";
        List<Map<String, Object>> records = dynamicToolMapper.findAll()
                .stream()
                .filter(item -> matches(item, normalizedKeyword))
                .map(this::toSimpleMap)
                .toList();

        Map<String, Object> result = success("查询成功");
        result.put("total", records.size());
        result.put("records", records);
        return result;
    }

    @Tool(name = "update_dynamic_tool_script",
            description = "更新动态 MCP Tool 的 Groovy 脚本。适用于 Tool 已创建但需要调整编排逻辑时使用，可选同时更新 input_schema 和 linked_request_keys。")
    public Map<String, Object> updateDynamicToolScript(
            @ToolParam(description = "动态 Tool 主键 id，与 tool_name 二选一", required = false)
            Long id,
            @ToolParam(description = "动态 Tool 名称，与 id 二选一", required = false)
            String tool_name,
            @ToolParam(description = "新的 Groovy 脚本", required = true)
            String groovy_script,
            @ToolParam(description = "新的 inputSchema JSON 对象，不传则保留原值", required = false)
            String input_schema,
            @ToolParam(description = "新的 API 白名单 JSON 数组，不传则保留原值", required = false)
            String linked_request_keys
    ) {
        if (McpUserContextHolder.getUserId() == null) {
            return failure("请先使用 MCP Token 调用");
        }

        try {
            validateRequired(groovy_script, "groovy_script 不能为空");

            McpDynamicToolEntity entity = resolveTool(id, tool_name);
            if (entity == null) {
                return failure("动态 Tool 不存在");
            }

            entity.setGroovyScript(groovy_script);
            if (input_schema != null && !input_schema.isBlank()) {
                validateJsonObject(input_schema, "input_schema 必须是 JSON 对象");
                entity.setInputSchema(input_schema);
            }
            if (linked_request_keys != null && !linked_request_keys.isBlank()) {
                List<String> requestKeys = parseRequestKeys(linked_request_keys);
                validateRequestKeys(requestKeys);
                entity.setLinkedRequestKeys(toJson(requestKeys));
            }

            dynamicToolMapper.update(entity);

            Map<String, Object> result = success("动态 Tool 脚本已更新");
            result.put("id", entity.getId());
            result.put("tool_name", entity.getToolName());
            result.put("publish_status", entity.getPublishStatus());
            return result;
        } catch (Exception e) {
            return failure(e.getMessage());
        }
    }

    private McpDynamicToolEntity resolveTool(Long id, String toolName) {
        if (id != null) {
            return dynamicToolMapper.findById(id);
        }
        if (toolName != null && !toolName.isBlank()) {
            return dynamicToolMapper.findByName(toolName.trim());
        }
        return null;
    }

    private boolean matches(McpDynamicToolEntity entity, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        return contains(entity.getToolName(), keyword)
                || contains(entity.getToolDescription(), keyword)
                || contains(entity.getLinkedRequestKeys(), keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private void validateRequestKeys(List<String> requestKeys) {
        for (String key : requestKeys) {
            McpRequestConfigEntity config = requestConfigMapper.findByConfigKey(key);
            if (config == null) {
                throw new IllegalArgumentException("API 配置不存在: " + key);
            }
            if (config.getPublishStatus() == null || config.getPublishStatus() == 0) {
                throw new IllegalArgumentException("API 配置尚未上线: " + key);
            }
        }
    }

    private List<String> parseRequestKeys(String json) throws Exception {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        return objectMapper.readValue(json, new TypeReference<>() {
        });
    }

    private void validateJsonObject(String json, String message) throws Exception {
        objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String defaultInputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {}
                }
                """;
    }

    private String defaultScript(List<String> requestKeys) {
        if (requestKeys == null || requestKeys.isEmpty()) {
            return """
                    return [
                        message: "动态工具已创建，请补充 API 白名单和 Groovy 脚本",
                        params: params
                    ]
                    """;
        }

        String key = requestKeys.get(0);
        return """
                def result = runRequest.runRequest("%s", params)

                return [
                    message: "API 调用完成",
                    result: result
                ]
                """.formatted(key);
    }

    private String firstNotBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private Map<String, Object> toSimpleMap(McpDynamicToolEntity entity) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", entity.getId());
        item.put("tool_name", entity.getToolName());
        item.put("tool_description", entity.getToolDescription());
        item.put("linked_request_keys", entity.getLinkedRequestKeys());
        item.put("publish_status", entity.getPublishStatus());
        item.put("is_enabled", entity.getEnabled());
        return item;
    }

    private Map<String, Object> success(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", message);
        return result;
    }

    private Map<String, Object> failure(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}
