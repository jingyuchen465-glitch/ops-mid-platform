package com.bear.mcp.single.core.tools;

import com.bear.mcp.single.core.context.McpUserContextHolder;
import com.bear.mcp.single.core.entity.McpResourceEntity;
import com.bear.mcp.single.core.mapper.McpResourceMapper;
import com.bear.mcp.single.core.storage.TosStorageService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** 通过 MCP Tool 直接创建 Markdown Resource。 */
@Component
public class ResourceStudioTools {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PRIVATE = 1;
    private static final int STATUS_PUBLIC = 2;
    private static final String MIME_MARKDOWN = "text/markdown";
    private static final long MAX_CONTENT_SIZE = 2 * 1024 * 1024L;

    private final McpResourceMapper resourceMapper;
    private final TosStorageService tosStorageService;

    public ResourceStudioTools(McpResourceMapper resourceMapper,
                               TosStorageService tosStorageService) {
        this.resourceMapper = resourceMapper;
        this.tosStorageService = tosStorageService;
    }

    @Tool(name = "create_resource",
            description = "直接创建或更新 Markdown MCP Resource。适合 Agent 批量上传表结构、指标口径、业务知识等 Resource 内容。默认创建草稿，发布后才会进入 resources/list 和 resources/read。")
    @Transactional
    public Map<String, Object> createResource(
            @ToolParam(description = "Resource URI，全局唯一，如 bear://yanque/text-to-sql/schema/student", required = true)
            String resource_uri,
            @ToolParam(description = "Resource 名称", required = true)
            String name,
            @ToolParam(description = "Resource 描述", required = false)
            String description,
            @ToolParam(description = "Markdown 正文内容", required = true)
            String markdown_content,
            @ToolParam(description = "发布状态：0 草稿，1 已上线不公开，2 已上线公开。默认 0", required = false)
            Integer publish_status,
            @ToolParam(description = "Resource URI 已存在时是否覆盖更新。默认 false", required = false)
            Boolean overwrite
    ) {
        Long userId = McpUserContextHolder.getUserId();
        if (userId == null) {
            return failure("请先使用 MCP Token 调用");
        }

        try {
            String uri = normalizeUri(resource_uri);
            validateRequired(name, "name 不能为空");
            validateRequired(markdown_content, "markdown_content 不能为空");
            validateContentSize(markdown_content);
            int publishStatus = normalizePublishStatus(publish_status);

            McpResourceEntity existed = resourceMapper.findByUri(uri);
            if (existed != null && !Boolean.TRUE.equals(overwrite)) {
                return failure("Resource URI 已存在，如需覆盖请传 overwrite=true: " + uri);
            }
            if (existed != null && !userId.equals(existed.getCreatorId())) {
                return failure("不能覆盖其他用户创建的 Resource: " + uri);
            }

            String objectKey = buildObjectKey(userId, uri);
            tosStorageService.uploadString(objectKey, markdown_content);

            McpResourceEntity entity = new McpResourceEntity();
            entity.setId(existed != null ? existed.getId() : null);
            entity.setResourceUri(uri);
            entity.setName(name.trim());
            entity.setDescription(description != null ? description.trim() : "");
            entity.setMimeType(MIME_MARKDOWN);
            entity.setObjectKey(objectKey);
            entity.setFileName(fileNameOf(uri));
            entity.setFileSize((long) markdown_content.getBytes(StandardCharsets.UTF_8).length);
            entity.setCreatorId(userId);
            entity.setPublishStatus(publishStatus);
            entity.setEnabled(publishStatus == STATUS_DRAFT ? 0 : 1);

            if (existed == null) {
                resourceMapper.insert(entity);
            } else {
                resourceMapper.update(entity);
            }

            Map<String, Object> result = success(existed == null ? "Resource 创建成功" : "Resource 已覆盖更新");
            result.put("id", entity.getId());
            result.put("resource_uri", entity.getResourceUri());
            result.put("name", entity.getName());
            result.put("object_key", entity.getObjectKey());
            result.put("file_name", entity.getFileName());
            result.put("file_size", entity.getFileSize());
            result.put("publish_status", entity.getPublishStatus());
            result.put("enabled", entity.getEnabled());
            return result;
        } catch (Exception e) {
            return failure(e.getMessage());
        }
    }

    private String normalizeUri(String value) {
        validateRequired(value, "resource_uri 不能为空");
        String uri = value.trim();
        if (!uri.matches("^[a-zA-Z][a-zA-Z0-9_:/.-]{1,254}$")) {
            throw new IllegalArgumentException("Resource URI 只能包含字母、数字、下划线、中划线、点、冒号和斜杠，并以字母开头");
        }
        return uri;
    }

    private void validateContentSize(String content) {
        long size = content.getBytes(StandardCharsets.UTF_8).length;
        if (size > MAX_CONTENT_SIZE) {
            throw new IllegalArgumentException("Markdown 内容不能超过 2MB");
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

    private String buildObjectKey(Long creatorId, String resourceUri) {
        String key = resourceUri.replaceFirst("^[a-zA-Z][a-zA-Z0-9+.-]*://", "");
        key = key.replace('\\', '/').replaceAll("[^a-zA-Z0-9_./-]", "_").replaceAll("/+", "/");
        while (key.startsWith("/")) {
            key = key.substring(1);
        }
        if (!key.toLowerCase(Locale.ROOT).endsWith(".md")) {
            key = key + ".md";
        }
        return "resource/" + creatorId + "/" + key;
    }

    private String fileNameOf(String resourceUri) {
        String value = resourceUri;
        int slash = value.lastIndexOf('/');
        if (slash >= 0 && slash < value.length() - 1) {
            value = value.substring(slash + 1);
        }
        value = value.replaceAll("[^a-zA-Z0-9_.-]", "_");
        if (!value.toLowerCase(Locale.ROOT).endsWith(".md")) {
            value = value + ".md";
        }
        return value;
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
