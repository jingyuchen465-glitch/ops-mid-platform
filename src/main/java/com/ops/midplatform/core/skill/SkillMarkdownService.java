package com.ops.midplatform.core.skill;

import com.ops.midplatform.core.entity.McpSkillEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Skill Markdown frontmatter 解析和补全。 */
@Service
public class SkillMarkdownService {
    private static final Pattern FRONTMATTER_PATTERN = Pattern.compile("^---\\s*\\n([\\s\\S]*?)\\n---\\s*\\n?([\\s\\S]*)$");
    private static final Pattern FIELD_PATTERN = Pattern.compile("^([a-zA-Z0-9_-]+):\\s*(.*)$");

    public String ensureFrontmatter(String markdown, McpSkillEntity skill) {
        String normalized = normalize(markdown);
        Frontmatter frontmatter = parse(normalized);
        if (frontmatter == null) {
            return buildFrontmatter(skill.getName(), skill.getDescription()) + "\n" + normalized.stripLeading();
        }
        if (StringUtils.hasText(frontmatter.fields().get("name"))
                && StringUtils.hasText(frontmatter.fields().get("description"))) {
            return normalized;
        }
        Map<String, String> fields = new LinkedHashMap<>(frontmatter.fields());
        fields.putIfAbsent("name", safe(skill.getName()));
        fields.putIfAbsent("description", safe(skill.getDescription()));
        if (!StringUtils.hasText(fields.get("name"))) {
            fields.put("name", safe(skill.getName()));
        }
        if (!StringUtils.hasText(fields.get("description"))) {
            fields.put("description", safe(skill.getDescription()));
        }
        return renderFrontmatter(fields) + "\n" + frontmatter.body().stripLeading();
    }

    public Metadata metadataOf(String markdown) {
        Frontmatter frontmatter = parse(normalize(markdown));
        if (frontmatter == null) {
            return new Metadata(null, null);
        }
        return new Metadata(frontmatter.fields().get("name"), frontmatter.fields().get("description"));
    }

    private Frontmatter parse(String markdown) {
        if (!StringUtils.hasText(markdown)) {
            return null;
        }
        Matcher matcher = FRONTMATTER_PATTERN.matcher(markdown.stripLeading());
        if (!matcher.matches()) {
            return null;
        }
        Map<String, String> fields = new LinkedHashMap<>();
        for (String line : matcher.group(1).split("\\n")) {
            Matcher fieldMatcher = FIELD_PATTERN.matcher(line.trim());
            if (fieldMatcher.matches()) {
                fields.put(fieldMatcher.group(1), unquote(fieldMatcher.group(2).trim()));
            }
        }
        return new Frontmatter(fields, matcher.group(2));
    }

    private String buildFrontmatter(String name, String description) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("name", safe(name));
        fields.put("description", safe(description));
        return renderFrontmatter(fields);
    }

    private String renderFrontmatter(Map<String, String> fields) {
        StringBuilder builder = new StringBuilder("---\n");
        fields.forEach((key, value) -> builder.append(key).append(": ").append(safe(value)).append("\n"));
        builder.append("---\n");
        return builder.toString();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value;
        if (normalized.startsWith("\uFEFF")) {
            normalized = normalized.substring(1);
        }
        return normalized.replace("\r\n", "\n").replace('\r', '\n');
    }

    private String unquote(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\n", " ").trim();
    }

    public record Metadata(String name, String description) {
    }

    private record Frontmatter(Map<String, String> fields, String body) {
    }
}
