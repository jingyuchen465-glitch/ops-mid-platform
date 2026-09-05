package com.bear.mcp.single.core.tools;

import com.bear.mcp.single.core.entity.McpSkillEntity;
import com.bear.mcp.single.core.mapper.McpSkillMapper;
import com.bear.mcp.single.core.skill.SkillMarkdownService;
import com.bear.mcp.single.core.storage.TosStorageService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 按 Skill ID 获取已上线 Skill，用于 Agent 安装到 Cursor 本地。 */
@Component
public class GetSkillTool {
    private static final String SKILL_MD = "SKILL.md";

    private final McpSkillMapper skillMapper;
    private final TosStorageService tosStorageService;
    private final SkillMarkdownService skillMarkdownService;

    public GetSkillTool(McpSkillMapper skillMapper,
                        TosStorageService tosStorageService,
                        SkillMarkdownService skillMarkdownService) {
        this.skillMapper = skillMapper;
        this.tosStorageService = tosStorageService;
        this.skillMarkdownService = skillMarkdownService;
    }

    @Tool(name = "get_skill",
            description = "按 skillCode（SKILL+10位数字）获取 Bear Skill 的完整 SKILL.md 内容，用于安装到 Cursor。本工具只返回已上线且启用的 Skill。返回 files 数组后，请将每个文件写入 .cursor/skills/{installDir}/{path}。")
    public Map<String, Object> getSkill(
            @ToolParam(description = "Skill ID，格式为 SKILL+10位数字，如 SKILL0000000001", required = true)
            String skillCode
    ) {
        if (skillCode == null || skillCode.isBlank()) {
            return failure("skillCode 不能为空");
        }
        McpSkillEntity skill = skillMapper.findEnabledByCode(skillCode.trim().toUpperCase());
        if (skill == null) {
            return failure("Skill 不存在、未上线或已禁用: " + skillCode);
        }
        String markdown = tosStorageService.downloadString(skill.getObjectKey());
        String content = skillMarkdownService.ensureFrontmatter(markdown, skill);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("skillCode", skill.getSkillCode());
        result.put("name", skill.getName());
        result.put("installDir", installDir(skill));
        result.put("description", skill.getDescription() != null ? skill.getDescription() : "");
        result.put("files", List.of(Map.of("path", SKILL_MD, "content", content)));
        return result;
    }

    private Map<String, Object> failure(String message) {
        return Map.of("success", false, "error", message);
    }

    private String installDir(McpSkillEntity skill) {
        String value = skill.getName() != null && !skill.getName().isBlank() ? skill.getName() : skill.getSkillCode();
        return value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }
}
