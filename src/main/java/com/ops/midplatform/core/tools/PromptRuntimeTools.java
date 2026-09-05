package com.bear.mcp.single.core.tools;

import com.bear.mcp.single.core.audit.AuditLogService;
import com.bear.mcp.single.core.context.McpUserContext;
import com.bear.mcp.single.core.context.McpUserContextHolder;
import com.bear.mcp.single.core.entity.McpPromptTemplateEntity;
import com.bear.mcp.single.core.prompt.PromptAccessService;
import com.bear.mcp.single.core.prompt.PromptTemplateService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/** 把 MCP Prompt 渲染能力桥接成 Tool，方便不主动触发 prompts/get 的客户端使用。 */
@Component
public class PromptRuntimeTools {

    private final PromptTemplateService promptTemplateService;
    private final PromptAccessService promptAccessService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public PromptRuntimeTools(PromptTemplateService promptTemplateService,
                              PromptAccessService promptAccessService,
                              AuditLogService auditLogService,
                              ObjectMapper objectMapper) {
        this.promptTemplateService = promptTemplateService;
        this.promptAccessService = promptAccessService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Tool(name = "render_prompt",
            description = "渲染一个已发布且当前 Token 有权限的 MCP Prompt。适合 Cursor 等不会自动调用 prompts/get 的客户端：先用本工具获取完整 Prompt 内容，再按 Prompt 指令继续调用 Resource 和 Tool。")
    public Map<String, Object> renderPrompt(
            @ToolParam(description = "MCP Prompt 名称，如 yanque_text_to_sql", required = true)
            String prompt_name,
            @ToolParam(description = "Prompt 参数 JSON 对象字符串，如 {\"question\":\"本月销售额\",\"datasource_id\":\"1\",\"max_rows\":\"100\",\"execute_sql\":\"true\"}", required = false)
            String arguments_json
    ) {
        long startedAt = System.currentTimeMillis();
        McpUserContext context = McpUserContextHolder.get();
        String promptName = prompt_name == null ? "" : prompt_name.trim();
        Map<String, Object> arguments = Map.of();
        String status = "SUCCESS";
        String errorMessage = null;
        Map<String, Object> result = Map.of();
        try {
            arguments = parseArguments(arguments_json);
            if (promptName.isBlank()) {
                throw new IllegalArgumentException("prompt_name 不能为空");
            }
            McpPromptTemplateEntity entity = promptTemplateService.findEnabledByName(promptName);
            if (entity == null) {
                throw new IllegalArgumentException("Prompt 不存在或未发布: " + promptName);
            }
            if (!promptAccessService.canAccess(context, promptName)) {
                throw new IllegalArgumentException("无权限使用 Prompt: " + promptName);
            }

            result = success(entity, arguments);
            return result;
        } catch (Exception e) {
            status = "ERROR";
            errorMessage = e.getMessage();
            result = failure(errorMessage);
            return result;
        } finally {
            auditLogService.recordToolCall(
                    context != null ? context.userId() : null,
                    context != null ? context.userName() : null,
                    "PROMPT:" + (promptName.isBlank() ? "<unknown>" : promptName),
                    status,
                    System.currentTimeMillis() - startedAt,
                    toJson(Map.of(
                            "method", "render_prompt",
                            "name", promptName,
                            "arguments", arguments
                    )),
                    toJson(result),
                    errorMessage
            );
        }
    }

    private Map<String, Object> success(McpPromptTemplateEntity entity, Map<String, Object> arguments) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("prompt_name", entity.getPromptName());
        result.put("title", entity.getTitle());
        result.put("description", entity.getDescription());
        result.put("arguments", arguments);
        result.put("linked_tool_names", safeJson(entity.getLinkedToolNames()));
        result.put("rendered_prompt", promptTemplateService.render(entity.getTemplateContent(), arguments));
        return result;
    }

    private Map<String, Object> failure(String message) {
        return Map.of("success", false, "error", message == null ? "Prompt 渲染失败" : message);
    }

    private Map<String, Object> parseArguments(String json) {
        try {
            if (json == null || json.isBlank()) {
                return Map.of();
            }
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("arguments_json 必须是 JSON 对象字符串");
        }
    }

    private Object safeJson(String json) {
        try {
            if (json == null || json.isBlank()) {
                return objectMapper.createArrayNode();
            }
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return json;
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }
}
