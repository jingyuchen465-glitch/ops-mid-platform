package com.ops.midplatform.core.prompt;

import com.ops.midplatform.common.exception.BusinessException;
import com.ops.midplatform.core.entity.McpPromptTemplateEntity;
import com.ops.midplatform.core.mapper.McpPromptTemplateMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** MCP Prompt 模板运行服务。 */
@Service
public class PromptTemplateService {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([A-Za-z_][A-Za-z0-9_.-]*)\\s*}}");

    private final McpPromptTemplateMapper promptMapper;
    private final ObjectMapper objectMapper;

    public PromptTemplateService(McpPromptTemplateMapper promptMapper, ObjectMapper objectMapper) {
        this.promptMapper = promptMapper;
        this.objectMapper = objectMapper;
    }

    public List<McpPromptTemplateEntity> listPublished() {
        return promptMapper.findPublished();
    }

    public McpPromptTemplateEntity findEnabledByName(String promptName) {
        if (promptName == null || promptName.isBlank()) {
            return null;
        }
        return promptMapper.findEnabledByName(promptName);
    }

    public String render(String template, Map<String, Object> arguments) {
        if (template == null) {
            return "";
        }
        Map<String, Object> safeArguments = arguments != null ? arguments : Map.of();
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = safeArguments.get(key);
            matcher.appendReplacement(result, Matcher.quoteReplacement(value != null ? String.valueOf(value) : ""));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public List<Map<String, Object>> parseArgumentsSchema(String json) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new BusinessException(400, "参数 Schema JSON 格式不正确");
        }
    }

    public String normalizeArgumentsSchema(String json) {
        return toJson(parseArgumentsSchema(json));
    }

    public List<String> parseToolNames(String json) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new BusinessException(400, "工具选择 JSON 格式不正确");
        }
    }

    public String normalizeToolNames(String json) {
        return toJson(parseToolNames(json));
    }

    public Map<String, Object> defaultArguments(List<Map<String, Object>> schema) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map<String, Object> item : schema) {
            Object name = item.get("name");
            if (name == null) {
                continue;
            }
            Object defaultValue = item.get("defaultValue");
            result.put(String.valueOf(name), defaultValue != null ? defaultValue : "");
        }
        return result;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BusinessException(400, "JSON序列化失败");
        }
    }
}
