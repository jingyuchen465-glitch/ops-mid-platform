package com.ops.midplatform.core.skill;

import com.ops.midplatform.core.entity.McpSkillEntity;
import com.ops.midplatform.core.mapper.McpSkillMapper;
import com.ops.midplatform.core.storage.TosStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** 将已上线单文件 Skill 打包成 Cursor 可安装 ZIP。 */
@Service
public class SkillPackageService {
    private final McpSkillMapper skillMapper;
    private final TosStorageService tosStorageService;
    private final SkillMarkdownService skillMarkdownService;

    public SkillPackageService(McpSkillMapper skillMapper,
                               TosStorageService tosStorageService,
                               SkillMarkdownService skillMarkdownService) {
        this.skillMapper = skillMapper;
        this.tosStorageService = tosStorageService;
        this.skillMarkdownService = skillMarkdownService;
    }

    public SkillPackage exportZip(String skillCode) {
        if (!StringUtils.hasText(skillCode)) {
            throw new IllegalArgumentException("skillCode 不能为空");
        }
        McpSkillEntity skill = skillMapper.findEnabledByCode(skillCode.trim().toUpperCase());
        if (skill == null) {
            throw new IllegalArgumentException("Skill 不存在、未上线或已禁用");
        }
        String markdown = tosStorageService.downloadString(skill.getObjectKey());
        String content = skillMarkdownService.ensureFrontmatter(markdown, skill);
        String installDir = installDir(skill);
        return new SkillPackage(skill.getSkillCode() + ".zip", zip(installDir, content));
    }

    private byte[] zip(String installDir, String content) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
                ZipEntry entry = new ZipEntry(installDir + "/SKILL.md");
                zip.putNextEntry(entry);
                zip.write((content != null ? content : "").getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Skill ZIP 打包失败: " + e.getMessage(), e);
        }
    }

    private String installDir(McpSkillEntity skill) {
        String value = StringUtils.hasText(skill.getName()) ? skill.getName() : skill.getSkillCode();
        String sanitized = value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return StringUtils.hasText(sanitized) ? sanitized : skill.getSkillCode();
    }

    public record SkillPackage(String filename, byte[] bytes) {
    }
}
