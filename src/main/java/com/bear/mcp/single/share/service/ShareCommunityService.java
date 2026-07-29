package com.bear.mcp.single.share.service;

import com.bear.mcp.single.core.entity.McpDynamicToolEntity;
import com.bear.mcp.single.core.entity.McpRequestConfigEntity;
import com.bear.mcp.single.core.mapper.McpDynamicToolMapper;
import com.bear.mcp.single.core.mapper.McpRequestConfigMapper;
import com.bear.mcp.single.share.res.ShareCommunityApiRes;
import com.bear.mcp.single.share.res.ShareCommunityToolRes;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bear 社区公开能力服务。
 *
 * <p>社区只负责“发现公开能力”，不承接 Token 配置和权限选择。</p>
 */
@Service
public class ShareCommunityService {

    /**
     * API 请求配置 Mapper。
     */
    private final McpRequestConfigMapper requestConfigMapper;

    /**
     * 动态工具 Mapper。
     */
    private final McpDynamicToolMapper dynamicToolMapper;

    /**
     * Spring 容器，用于扫描实际注册的 @Tool 方法。
     */
    private final ApplicationContext applicationContext;

    public ShareCommunityService(McpRequestConfigMapper requestConfigMapper,
                                 McpDynamicToolMapper dynamicToolMapper,
                                 ApplicationContext applicationContext) {
        this.requestConfigMapper = requestConfigMapper;
        this.dynamicToolMapper = dynamicToolMapper;
        this.applicationContext = applicationContext;
    }

    /**
     * 查询公开的 MCP Tools。
     */
    public List<ShareCommunityToolRes> listTools() {
        return dynamicToolMapper.findPublicTools()
                .stream()
                .map(this::toToolRes)
                .toList();
    }

    /**
     * 查询当前单体项目真实内置的 MCP Tools。
     */
    public List<ShareCommunityToolRes> listBuiltinTools() {
        List<ShareCommunityToolRes> result = new ArrayList<>();
        Map<String, ShareCommunityToolRes> byToolName = new LinkedHashMap<>();

        for (String beanName : applicationContext.getBeanNamesForType(Object.class, false, false)) {
            Class<?> beanType = applicationContext.getType(beanName);
            if (beanType == null || !beanType.getPackageName().startsWith("com.bear.mcp.single")) {
                continue;
            }

            for (Method method : beanType.getMethods()) {
                Tool tool = method.getAnnotation(Tool.class);
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
        return res;
    }

    private String resolveBuiltinCategory(Class<?> beanType) {
        if (beanType.getSimpleName().contains("Calculator")) {
            return "计算工具";
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
}
