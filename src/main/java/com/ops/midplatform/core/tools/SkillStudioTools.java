package com.bear.mcp.single.core.tools;

import com.bear.mcp.single.core.context.McpUserContextHolder;
import com.bear.mcp.single.core.entity.McpSkillEntity;
import com.bear.mcp.single.core.mapper.McpSkillMapper;
import com.bear.mcp.single.core.skill.SkillMarkdownService;
import com.bear.mcp.single.core.storage.TosStorageService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** 通过 MCP Tool 直接创建 Skill。 */
@Component
public class SkillStudioTools {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PRIVATE = 1;
    private static final int STATUS_PUBLIC = 2;
    private static final String SKILL_MD = "SKILL.md";
    private static final long MAX_CONTENT_SIZE = 2 * 1024 * 1024L;

    private final McpSkillMapper skillMapper;
    private final TosStorageService tosStorageService;
    private final SkillMarkdownService skillMarkdownService;

    public SkillStudioTools(McpSkillMapper skillMapper,
                            TosStorageService tosStorageService,
                            SkillMarkdownService skillMarkdownService) {
        this.skillMapper = skillMapper;
        this.tosStorageService = tosStorageService;
        this.skillMarkdownService = skillMarkdownService;
    }

    @Tool(name = "create_skill",
            description = "直接创建或更新 Bear Skill。适合 Agent 把 SKILL.md 内容发布为可通过 get_skill 安装的 Skill，支持自动生成 skill_code、补全 frontmatter 和发布状态。默认创建草稿。")
    @Transactional
    public Map<String, Object> createSkill(
            @ToolParam(description = "Skill ID，格式为 SKILL+10位数字。不传则自动生成下一个 ID", required = false)
            String skill_code,
            @ToolParam(description = "Skill 名称。不传时优先使用 SKILL.md frontmatter 的 name，再回退为 skill_code", required = false)
            String name,
            @ToolParam(description = "Skill 描述。不传时优先使用 SKILL.md frontmatter 的 description", required = false)
            String description,
            @ToolParam(description = "Skill 分类，如 Text-to-SQL、数据分析、研发工具", required = false)
            String category,
            @ToolParam(description = "完整 SKILL.md 正文内容", required = true)
            String markdown_content,
            @ToolParam(description = "发布状态：0 草稿，1 已上线不公开，2 已上线公开。默认 0", required = false)
            Integer publish_status,
            @ToolParam(description = "skill_code 已存在时是否覆盖更新。默认 false", required = false)
            Boolean overwrite
    ) {
        Long userId = McpUserContextHolder.getUserId();
        if (userId == null) {
            return failure("请先使用 MCP Token 调用");
        }

        try {
            validateRequired(markdown_content, "markdown_content 不能为空");
            validateContentSize(markdown_content);

            String skillCode = StringUtils.hasText(skill_code) ? normalizeSkillCode(skill_code) : nextSkillCode();
            int publishStatus = normalizePublishStatus(publish_status);
            SkillMarkdownService.Metadata metadata = skillMarkdownService.metadataOf(markdown_content);
            String skillName = firstText(name, metadata.name(), skillCode);
            String skillDescription = firstText(description, metadata.description(), "");

            McpSkillEntity existed = skillMapper.findByCode(skillCode);
            if (existed != null && !Boolean.TRUE.equals(overwrite)) {
                return failure("Skill ID 已存在，如需覆盖请传 overwrite=true: " + skillCode);
            }
            if (existed != null && !userId.equals(existed.getCreatorId())) {
                return failure("不能覆盖其他用户创建的 Skill: " + skillCode);
            }

            String objectKey = "skills/" + skillCode + "/" + SKILL_MD;
            McpSkillEntity entity = new McpSkillEntity();
            entity.setId(existed != null ? existed.getId() : null);
            entity.setSkillCode(skillCode);
            entity.setName(skillName);
            entity.setDescription(skillDescription);
            entity.setCategory(category != null ? category.trim() : "");
            entity.setObjectKey(objectKey);
            entity.setFileName(SKILL_MD);
            entity.setCreatorId(userId);
            entity.setPublishStatus(publishStatus);
            entity.setEnabled(publishStatus == STATUS_DRAFT ? 0 : 1);

            String content = skillMarkdownService.ensureFrontmatter(markdown_content, entity);
            entity.setFileSize((long) content.getBytes(StandardCharsets.UTF_8).length);
            validateContentSize(content);
            tosStorageService.uploadString(objectKey, content);

            if (existed == null) {
                skillMapper.insert(entity);
            } else {
                skillMapper.update(entity);
            }

            Map<String, Object> result = success(existed == null ? "Skill 创建成功" : "Skill 已覆盖更新");
            result.put("id", entity.getId());
            result.put("skill_code", entity.getSkillCode());
            result.put("name", entity.getName());
            result.put("description", entity.getDescription());
            result.put("category", entity.getCategory());
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

    private String nextSkillCode() {
        String max = skillMapper.findMaxSkillCode();
        long next = 1L;
        if (StringUtils.hasText(max)) {
            next = Long.parseLong(max.substring("SKILL".length())) + 1;
        }
        return "SKILL" + String.format("%010d", next);
    }

    private String normalizeSkillCode(String value) {
        validateRequired(value, "skill_code 不能为空");
        String skillCode = value.trim().toUpperCase(Locale.ROOT);
        if (!skillCode.matches("^SKILL[0-9]{10}$")) {
            throw new IllegalArgumentException("skill_code 格式必须为 SKILL+10位数字");
        }
        return skillCode;
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

    private void validateContentSize(String content) {
        long size = content.getBytes(StandardCharsets.UTF_8).length;
        if (size > MAX_CONTENT_SIZE) {
            throw new IllegalArgumentException("Skill Markdown 内容不能超过 2MB");
        }
    }

    private void validateRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
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
