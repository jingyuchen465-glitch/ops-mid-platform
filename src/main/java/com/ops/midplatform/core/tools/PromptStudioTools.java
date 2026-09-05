package com.bear.mcp.single.core.tools;

import com.bear.mcp.single.core.context.McpUserContextHolder;
import com.bear.mcp.single.core.entity.McpPromptTemplateEntity;
import com.bear.mcp.single.core.mapper.McpPromptTemplateMapper;
import com.bear.mcp.single.core.prompt.PromptTemplateService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** 通过 MCP Tool 直接创建 Prompt 模板。 */
@Component
public class PromptStudioTools {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PRIVATE = 1;
    private static final int STATUS_PUBLIC = 2;
    private static final long MAX_TEMPLATE_SIZE = 2 * 1024 * 1024L;

    private final McpPromptTemplateMapper promptMapper;
    private final PromptTemplateService promptTemplateService;

    public PromptStudioTools(McpPromptTemplateMapper promptMapper,
                             PromptTemplateService promptTemplateService) {
        this.promptMapper = promptMapper;
        this.promptTemplateService = promptTemplateService;
    }

    @Tool(name = "create_prompt",
            description = "直接创建或更新 MCP Prompt 模板。适合 Agent 批量沉淀工作流 Prompt，支持参数 Schema、建议工具列表和发布状态。默认创建草稿。")
    @Transactional
    public Map<String, Object> createPrompt(
            @ToolParam(description = "MCP Prompt 名称，全局唯一，只能包含字母、数字、下划线，并以字母开头，如 yanque_text_to_sql", required = true)
            String prompt_name,
            @ToolParam(description = "Prompt 展示标题", required = true)
            String title,
            @ToolParam(description = "Prompt 描述", required = false)
            String description,
            @ToolParam(description = "Prompt 模板正文，支持 {{name}} 占位符", required = true)
            String template_content,
            @ToolParam(description = "MCP Prompt arguments JSON 数组，如 [{\"name\":\"question\",\"description\":\"用户问题\",\"required\":true}]", required = false)
            String arguments_schema,
            @ToolParam(description = "建议使用的工具名 JSON 数组，如 [\"query_data_source\"]", required = false)
            String linked_tool_names,
            @ToolParam(description = "发布状态：0 草稿，1 已上线不公开，2 已上线公开。默认 0", required = false)
            Integer publish_status,
            @ToolParam(description = "Prompt 名称已存在时是否覆盖更新。默认 false", required = false)
            Boolean overwrite
    ) {
        Long userId = McpUserContextHolder.getUserId();
        if (userId == null) {
            return failure("请先使用 MCP Token 调用");
        }

        try {
            String promptName = normalizeName(prompt_name);
            validateRequired(title, "title 不能为空");
            validateRequired(template_content, "template_content 不能为空");
            validateTemplateSize(template_content);
            int publishStatus = normalizePublishStatus(publish_status);

            String normalizedArgumentsSchema = promptTemplateService.normalizeArgumentsSchema(arguments_schema);
            String normalizedLinkedToolNames = promptTemplateService.normalizeToolNames(linked_tool_names);

            McpPromptTemplateEntity existed = promptMapper.findByName(promptName);
            if (existed != null && !Boolean.TRUE.equals(overwrite)) {
                return failure("Prompt 名称已存在，如需覆盖请传 overwrite=true: " + promptName);
            }
            if (existed != null && !userId.equals(existed.getCreatorId())) {
                return failure("不能覆盖其他用户创建的 Prompt: " + promptName);
            }

            McpPromptTemplateEntity entity = new McpPromptTemplateEntity();
            entity.setId(existed != null ? existed.getId() : null);
            entity.setPromptName(promptName);
            entity.setTitle(title.trim());
            entity.setDescription(description != null ? description.trim() : "");
            entity.setArgumentsSchema(normalizedArgumentsSchema);
            entity.setTemplateContent(template_content);
            entity.setLinkedToolNames(normalizedLinkedToolNames);
            entity.setCreatorId(userId);
            entity.setPublishStatus(publishStatus);
            entity.setEnabled(publishStatus == STATUS_DRAFT ? 0 : 1);

            if (existed == null) {
                promptMapper.insert(entity);
            } else {
                promptMapper.update(entity);
            }

            Map<String, Object> result = success(existed == null ? "Prompt 创建成功" : "Prompt 已覆盖更新");
            result.put("id", entity.getId());
            result.put("prompt_name", entity.getPromptName());
            result.put("title", entity.getTitle());
            result.put("arguments_schema", entity.getArgumentsSchema());
            result.put("linked_tool_names", entity.getLinkedToolNames());
            result.put("template_size", template_content.getBytes(StandardCharsets.UTF_8).length);
            result.put("publish_status", entity.getPublishStatus());
            result.put("enabled", entity.getEnabled());
            return result;
        } catch (Exception e) {
            return failure(e.getMessage());
        }
    }

    private String normalizeName(String value) {
        validateRequired(value, "prompt_name 不能为空");
        String name = value.trim();
        if (!name.matches("^[a-zA-Z][a-zA-Z0-9_]{1,127}$")) {
            throw new IllegalArgumentException("prompt_name 只能包含字母、数字、下划线，并以字母开头");
        }
        return name;
    }

    private void validateTemplateSize(String content) {
        long size = content.getBytes(StandardCharsets.UTF_8).length;
        if (size > MAX_TEMPLATE_SIZE) {
            throw new IllegalArgumentException("Prompt 模板内容不能超过 2MB");
        }
    }

    private int normalizePublishStatus(Integer publishStatus) {
        if (publishStatus == null) {
            return STATUS_DRAFT;
        }
        if (publishStatus != STATUS_DRAFT && publishStatus != STATUS_PRIVATE && publishStatus != STATUS_PUBLIC) {
            throw new IllegalArgumentException("publish_status 只能是 0、1 或 2");
        }
        return publishStatus;
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
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
