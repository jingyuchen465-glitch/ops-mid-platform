package com.bear.mcp.single.share.service;

import com.bear.mcp.single.core.entity.McpDynamicToolEntity;
import com.bear.mcp.single.core.entity.McpCommunityLikeStatEntity;
import com.bear.mcp.single.core.entity.McpPromptTemplateEntity;
import com.bear.mcp.single.core.entity.McpRequestConfigEntity;
import com.bear.mcp.single.core.entity.McpResourceEntity;
import com.bear.mcp.single.core.entity.McpSkillEntity;
import com.bear.mcp.single.core.mapper.McpCommunityLikeMapper;
import com.bear.mcp.single.core.mapper.McpDynamicToolMapper;
import com.bear.mcp.single.core.mapper.McpPromptTemplateMapper;
import com.bear.mcp.single.core.mapper.McpRequestConfigMapper;
import com.bear.mcp.single.core.mapper.McpResourceMapper;
import com.bear.mcp.single.core.mapper.McpSkillMapper;
import com.bear.mcp.single.core.storage.TosStorageService;
import com.bear.mcp.single.common.exception.BusinessException;
import com.bear.mcp.single.share.res.ShareCommunityApiRes;
import com.bear.mcp.single.share.res.ShareCommunityLikeRes;
import com.bear.mcp.single.share.res.ShareCommunityToolRes;
import com.bear.mcp.single.share.res.ShareStudioPromptRes;
import com.bear.mcp.single.share.res.ShareStudioResourceRes;
import com.bear.mcp.single.share.res.ShareStudioSkillRes;
import org.springframework.aop.support.AopUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bear 社区公开能力服务。
 *
 * <p>社区只负责“发现公开能力”，不承接 Token 配置和权限选择。</p>
 */
@Service
@Slf4j
public class ShareCommunityService {

    private static final String LIKE_SKILL = "SKILL";
    private static final String LIKE_PROMPT = "PROMPT";
    private static final String LIKE_RESOURCE = "RESOURCE";
    private static final String LIKE_DYNAMIC_TOOL = "DYNAMIC_TOOL";
    private static final String LIKE_BUILTIN_TOOL = "BUILTIN_TOOL";

    /**
     * API 请求配置 Mapper。
     */
    private final McpRequestConfigMapper requestConfigMapper;

    /**
     * 社区点赞 Mapper。
     */
    private final McpCommunityLikeMapper likeMapper;

    /**
     * 动态工具 Mapper。
     */
    private final McpDynamicToolMapper dynamicToolMapper;

    /**
     * Skill Mapper。
     */
    private final McpSkillMapper skillMapper;

    /**
     * Prompt Mapper。
     */
    private final McpPromptTemplateMapper promptMapper;

    /**
     * Resource Mapper。
     */
    private final McpResourceMapper resourceMapper;

    /**
     * TOS 存储服务。
     */
    private final TosStorageService tosStorageService;

    /**
     * Spring 容器，用于扫描实际注册的 @Tool 方法。
     */
    private final ApplicationContext applicationContext;

    public ShareCommunityService(McpRequestConfigMapper requestConfigMapper,
                                 McpCommunityLikeMapper likeMapper,
                                 McpDynamicToolMapper dynamicToolMapper,
                                 McpSkillMapper skillMapper,
                                 McpPromptTemplateMapper promptMapper,
                                 McpResourceMapper resourceMapper,
                                 TosStorageService tosStorageService,
                                 ApplicationContext applicationContext) {
        this.requestConfigMapper = requestConfigMapper;
        this.likeMapper = likeMapper;
        this.dynamicToolMapper = dynamicToolMapper;
        this.skillMapper = skillMapper;
        this.promptMapper = promptMapper;
        this.resourceMapper = resourceMapper;
        this.tosStorageService = tosStorageService;
        this.applicationContext = applicationContext;
    }

    /**
     * 查询公开的 MCP Tools。
     */
    public List<ShareCommunityToolRes> listTools(Long userId) {
        List<ShareCommunityToolRes> result = dynamicToolMapper.findPublicTools()
                .stream()
                .map(this::toToolRes)
                .toList();
        attachToolLikeState(result, userId, LIKE_DYNAMIC_TOOL);
        return result;
    }

    /**
     * 查询当前单体项目真实内置的 MCP Tools。
     */
    public List<ShareCommunityToolRes> listBuiltinTools(Long userId) {
        List<ShareCommunityToolRes> result = new ArrayList<>();
        Map<String, ShareCommunityToolRes> byToolName = new LinkedHashMap<>();

        for (String beanName : applicationContext.getBeanNamesForType(Object.class, false, false)) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanType = AopUtils.getTargetClass(bean);
            if (beanType == null || !beanType.getPackageName().startsWith("com.bear.mcp.single")) {
                continue;
            }

            for (Method method : beanType.getMethods()) {
                Tool tool = AnnotatedElementUtils.findMergedAnnotation(method, Tool.class);
                if (tool == null) {
                    continue;
                }

                ShareCommunityToolRes res = toBuiltinToolRes(beanType, method, tool);
                byToolName.putIfAbsent(res.getToolName(), res);
            }
        }

        result.addAll(byToolName.values());
        result.sort(Comparator.comparing(ShareCommunityToolRes::getToolName));
        for (int index = 0; index < result.size(); index++) {
            result.get(index).setId((long) index + 1);
        }
        attachToolLikeState(result, userId, LIKE_BUILTIN_TOOL);
        return result;
    }

    /**
     * 查询公开的 API 能力。
     */
    public List<ShareCommunityApiRes> listApis() {
        return requestConfigMapper.findPublicApis()
                .stream()
                .map(this::toApiRes)
                .toList();
    }

    /**
     * 查询公开的 Skills。
     */
    public List<ShareStudioSkillRes> listSkills(Long userId) {
        List<ShareStudioSkillRes> result = skillMapper.findPublished()
                .stream()
                .map(this::toSkillRes)
                .toList();
        attachSkillLikeState(result, userId);
        return result;
    }

    /**
     * 查询公开的 MCP Prompts。
     */
    public List<ShareStudioPromptRes> listPrompts(Long userId) {
        List<ShareStudioPromptRes> result = promptMapper.findPublished()
                .stream()
                .filter(entity -> Integer.valueOf(2).equals(entity.getPublishStatus()))
                .map(this::toPromptRes)
                .toList();
        attachPromptLikeState(result, userId);
        return result;
    }

    /**
     * 查询公开的 MCP Resources。
     */
    public List<ShareStudioResourceRes> listResources(Long userId) {
        List<ShareStudioResourceRes> result = resourceMapper.findPublished()
                .stream()
                .filter(entity -> Integer.valueOf(2).equals(entity.getPublishStatus()))
                .map(this::toResourceRes)
                .toList();
        attachResourceLikeState(result, userId);
        return result;
    }

    /**
     * 切换社区点赞状态。
     */
    public ShareCommunityLikeRes toggleLike(String targetType, String targetKey, Long userId) {
        String normalizedType = normalizeTargetType(targetType);
        String normalizedKey = normalizeTargetKey(targetKey);
        ensureLikeTargetExists(normalizedType, normalizedKey);

        boolean existed = likeMapper.exists(normalizedType, normalizedKey, userId) > 0;
        if (existed) {
            likeMapper.delete(normalizedType, normalizedKey, userId);
        } else {
            likeMapper.insertIgnore(normalizedType, normalizedKey, userId);
        }

        ShareCommunityLikeRes res = new ShareCommunityLikeRes();
        res.setTargetType(normalizedType);
        res.setTargetKey(normalizedKey);
        res.setLiked(!existed);
        res.setLikeCount(likeMapper.countOne(normalizedType, normalizedKey));
        return res;
    }

    /**
     * 读取公开 Resource 正文。
     */
    public String readResourceContent(Long id) {
        McpResourceEntity entity = resourceMapper.findById(id);
        if (entity == null || !Integer.valueOf(1).equals(entity.getEnabled())
                || !Integer.valueOf(2).equals(entity.getPublishStatus())) {
            return "";
        }
        return tosStorageService.downloadStringQuietly(entity.getObjectKey());
    }

    private ShareCommunityToolRes toToolRes(McpDynamicToolEntity entity) {
        ShareCommunityToolRes res = new ShareCommunityToolRes();
        res.setId(entity.getId());
        res.setToolName(entity.getToolName());
        res.setToolType("DYNAMIC");
        res.setCategory("动态工具");
        res.setScriptLanguage("Groovy");
        res.setToolDescription(entity.getToolDescription());
        res.setInputSchema(entity.getInputSchema());
        res.setGroovyScript(entity.getGroovyScript());
        res.setLinkedRequestKeys(entity.getLinkedRequestKeys());
        res.setLinkedDataSourceIds(entity.getLinkedDataSourceIds());
        res.setPublishStatus(entity.getPublishStatus());
        res.setLikeTargetType(LIKE_DYNAMIC_TOOL);
        res.setLikeTargetKey(String.valueOf(entity.getId()));
        return res;
    }

    private ShareCommunityToolRes toBuiltinToolRes(Class<?> beanType, Method method, Tool tool) {
        ShareCommunityToolRes res = new ShareCommunityToolRes();
        res.setToolName(tool.name() == null || tool.name().isBlank() ? method.getName() : tool.name());
        res.setToolType("BUILTIN");
        res.setCategory(resolveBuiltinCategory(beanType));
        res.setScriptLanguage("Java @Tool");
        res.setToolDescription(tool.description());
        res.setInputSchema(buildInputSchema(method));
        res.setGroovyScript("");
        res.setLinkedRequestKeys("[]");
        res.setLinkedDataSourceIds("[]");
        res.setPublishStatus(2);
        res.setLikeTargetType(LIKE_BUILTIN_TOOL);
        res.setLikeTargetKey(res.getToolName());
        return res;
    }

    private String resolveBuiltinCategory(Class<?> beanType) {
        if (beanType.getSimpleName().contains("Calculator")) {
            return "计算工具";
        }
        if (beanType.getSimpleName().contains("Resource")) {
            return "Resource 工具";
        }
        if (beanType.getSimpleName().contains("Prompt")) {
            return "Prompt 工具";
        }
        if (beanType.getSimpleName().contains("Skill")) {
            return "Skill 工具";
        }
        return "系统工具";
    }

    private String buildInputSchema(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\":\"object\",\"properties\":{");

        Parameter[] parameters = method.getParameters();
        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
            if (index > 0) {
                builder.append(",");
            }
            builder.append("\"").append(parameter.getName()).append("\":{");
            builder.append("\"type\":\"").append(jsonType(parameter.getType())).append("\"");
            if (toolParam != null && !toolParam.description().isBlank()) {
                builder.append(",\"description\":\"").append(escapeJson(toolParam.description())).append("\"");
            }
            builder.append("}");
        }

        builder.append("}");

        List<String> requiredNames = new ArrayList<>();
        for (Parameter parameter : parameters) {
            ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
            if (toolParam == null || toolParam.required()) {
                requiredNames.add(parameter.getName());
            }
        }
        if (!requiredNames.isEmpty()) {
            builder.append(",\"required\":[");
            for (int index = 0; index < requiredNames.size(); index++) {
                if (index > 0) {
                    builder.append(",");
                }
                builder.append("\"").append(requiredNames.get(index)).append("\"");
            }
            builder.append("]");
        }

        builder.append("}");
        return builder.toString();
    }

    private String jsonType(Class<?> type) {
        if (type == Integer.class || type == int.class || type == Long.class || type == long.class) {
            return "integer";
        }
        if (type == BigDecimal.class || type == Double.class || type == double.class
                || type == Float.class || type == float.class) {
            return "number";
        }
        if (type == Boolean.class || type == boolean.class) {
            return "boolean";
        }
        return "string";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private ShareCommunityApiRes toApiRes(McpRequestConfigEntity entity) {
        ShareCommunityApiRes res = new ShareCommunityApiRes();
        res.setId(entity.getId());
        res.setRequestId(entity.getRequestId());
        res.setConfigKey(entity.getConfigKey());
        res.setName(entity.getName());
        res.setType(entity.getType());
        res.setMethod(entity.getMethod());
        res.setDescription(entity.getDescription());
        res.setCategory(entity.getCategory());
        res.setPublishStatus(entity.getPublishStatus());
        return res;
    }

    private ShareStudioSkillRes toSkillRes(McpSkillEntity entity) {
        ShareStudioSkillRes res = new ShareStudioSkillRes();
        res.setId(entity.getId());
        res.setSkillCode(entity.getSkillCode());
        res.setName(entity.getName());
        res.setDescription(entity.getDescription());
        res.setCategory(entity.getCategory());
        res.setObjectKey(entity.getObjectKey());
        res.setFileName(entity.getFileName());
        res.setFileSize(entity.getFileSize());
        res.setCreatorId(entity.getCreatorId());
        res.setEnabled(entity.getEnabled());
        res.setPublishStatus(entity.getPublishStatus());
        res.setCreateTime(entity.getCreateTime());
        res.setUpdateTime(entity.getUpdateTime());
        res.setLikeTargetType(LIKE_SKILL);
        res.setLikeTargetKey(String.valueOf(entity.getId()));
        return res;
    }

    private ShareStudioPromptRes toPromptRes(McpPromptTemplateEntity entity) {
        ShareStudioPromptRes res = new ShareStudioPromptRes();
        res.setId(entity.getId());
        res.setPromptName(entity.getPromptName());
        res.setTitle(entity.getTitle());
        res.setDescription(entity.getDescription());
        res.setArgumentsSchema(entity.getArgumentsSchema());
        res.setTemplateContent(entity.getTemplateContent());
        res.setLinkedToolNames(entity.getLinkedToolNames());
        res.setCreatorId(entity.getCreatorId());
        res.setEnabled(entity.getEnabled());
        res.setPublishStatus(entity.getPublishStatus());
        res.setLikeTargetType(LIKE_PROMPT);
        res.setLikeTargetKey(String.valueOf(entity.getId()));
        return res;
    }

    private ShareStudioResourceRes toResourceRes(McpResourceEntity entity) {
        ShareStudioResourceRes res = new ShareStudioResourceRes();
        res.setId(entity.getId());
        res.setResourceUri(entity.getResourceUri());
        res.setName(entity.getName());
        res.setDescription(entity.getDescription());
        res.setMimeType(entity.getMimeType());
        res.setObjectKey(entity.getObjectKey());
        res.setFileName(entity.getFileName());
        res.setFileSize(entity.getFileSize());
        res.setCreatorId(entity.getCreatorId());
        res.setEnabled(entity.getEnabled());
        res.setPublishStatus(entity.getPublishStatus());
        res.setCreateTime(entity.getCreateTime());
        res.setUpdateTime(entity.getUpdateTime());
        res.setLikeTargetType(LIKE_RESOURCE);
        res.setLikeTargetKey(String.valueOf(entity.getId()));
        return res;
    }

    private void attachToolLikeState(List<ShareCommunityToolRes> items, Long userId, String targetType) {
        Map<String, Long> counts = likeCountMap(List.of(targetType));
        Set<String> likedKeys = likedKeys(targetType,
                items.stream().map(ShareCommunityToolRes::getLikeTargetKey).toList(),
                userId);
        for (ShareCommunityToolRes item : items) {
            item.setLikeCount(counts.getOrDefault(targetType + ":" + item.getLikeTargetKey(), 0L));
            item.setLiked(likedKeys.contains(item.getLikeTargetKey()));
        }
    }

    private void attachSkillLikeState(List<ShareStudioSkillRes> items, Long userId) {
        Map<String, Long> counts = likeCountMap(List.of(LIKE_SKILL));
        Set<String> likedKeys = likedKeys(LIKE_SKILL,
                items.stream().map(ShareStudioSkillRes::getLikeTargetKey).toList(),
                userId);
        for (ShareStudioSkillRes item : items) {
            item.setLikeCount(counts.getOrDefault(LIKE_SKILL + ":" + item.getLikeTargetKey(), 0L));
            item.setLiked(likedKeys.contains(item.getLikeTargetKey()));
        }
    }

    private void attachPromptLikeState(List<ShareStudioPromptRes> items, Long userId) {
        Map<String, Long> counts = likeCountMap(List.of(LIKE_PROMPT));
        Set<String> likedKeys = likedKeys(LIKE_PROMPT,
                items.stream().map(ShareStudioPromptRes::getLikeTargetKey).toList(),
                userId);
        for (ShareStudioPromptRes item : items) {
            item.setLikeCount(counts.getOrDefault(LIKE_PROMPT + ":" + item.getLikeTargetKey(), 0L));
            item.setLiked(likedKeys.contains(item.getLikeTargetKey()));
        }
    }

    private void attachResourceLikeState(List<ShareStudioResourceRes> items, Long userId) {
        Map<String, Long> counts = likeCountMap(List.of(LIKE_RESOURCE));
        Set<String> likedKeys = likedKeys(LIKE_RESOURCE,
                items.stream().map(ShareStudioResourceRes::getLikeTargetKey).toList(),
                userId);
        for (ShareStudioResourceRes item : items) {
            item.setLikeCount(counts.getOrDefault(LIKE_RESOURCE + ":" + item.getLikeTargetKey(), 0L));
            item.setLiked(likedKeys.contains(item.getLikeTargetKey()));
        }
    }

    private Map<String, Long> likeCountMap(List<String> targetTypes) {
        Map<String, Long> result = new HashMap<>();
        try {
            for (McpCommunityLikeStatEntity stat : likeMapper.countByTypes(targetTypes)) {
                result.put(stat.getTargetType() + ":" + stat.getTargetKey(), stat.getLikeCount());
            }
        } catch (RuntimeException exception) {
            log.warn("社区点赞统计读取失败，列表将按 0 点赞展示", exception);
        }
        return result;
    }

    private Set<String> likedKeys(String targetType, List<String> targetKeys, Long userId) {
        if (targetKeys == null || targetKeys.isEmpty()) {
            return Set.of();
        }
        try {
            return new HashSet<>(likeMapper.findLikedKeys(targetType, targetKeys, userId));
        } catch (RuntimeException exception) {
            log.warn("社区用户点赞状态读取失败，列表将按未点赞展示", exception);
            return Set.of();
        }
    }

    private String normalizeTargetType(String targetType) {
        if (targetType == null) {
            throw new BusinessException(400, "点赞对象类型不能为空");
        }
        String normalized = targetType.trim().toUpperCase();
        if (List.of(LIKE_SKILL, LIKE_PROMPT, LIKE_RESOURCE, LIKE_DYNAMIC_TOOL, LIKE_BUILTIN_TOOL).contains(normalized)) {
            return normalized;
        }
        throw new BusinessException(400, "不支持的点赞对象类型");
    }

    private String normalizeTargetKey(String targetKey) {
        if (targetKey == null || targetKey.trim().isEmpty()) {
            throw new BusinessException(400, "点赞对象不能为空");
        }
        return targetKey.trim();
    }

    private void ensureLikeTargetExists(String targetType, String targetKey) {
        boolean exists = switch (targetType) {
            case LIKE_SKILL -> {
                McpSkillEntity entity = skillMapper.findById(parseId(targetKey));
                yield entity != null && Integer.valueOf(1).equals(entity.getEnabled())
                        && Integer.valueOf(2).equals(entity.getPublishStatus());
            }
            case LIKE_PROMPT -> {
                McpPromptTemplateEntity entity = promptMapper.findById(parseId(targetKey));
                yield entity != null && Integer.valueOf(1).equals(entity.getEnabled())
                        && Integer.valueOf(2).equals(entity.getPublishStatus());
            }
            case LIKE_RESOURCE -> {
                McpResourceEntity entity = resourceMapper.findById(parseId(targetKey));
                yield entity != null && Integer.valueOf(1).equals(entity.getEnabled())
                        && Integer.valueOf(2).equals(entity.getPublishStatus());
            }
            case LIKE_DYNAMIC_TOOL -> {
                McpDynamicToolEntity entity = dynamicToolMapper.findById(parseId(targetKey));
                yield entity != null && Integer.valueOf(1).equals(entity.getEnabled())
                        && Integer.valueOf(2).equals(entity.getPublishStatus());
            }
            case LIKE_BUILTIN_TOOL -> listBuiltinTools(0L).stream()
                    .anyMatch(item -> targetKey.equals(item.getLikeTargetKey()));
            default -> false;
        };
        if (!exists) {
            throw new BusinessException(404, "点赞对象不存在或未公开");
        }
    }

    private Long parseId(String targetKey) {
        try {
            return Long.parseLong(targetKey);
        } catch (NumberFormatException exception) {
            throw new BusinessException(400, "点赞对象 ID 不合法");
        }
    }
}
