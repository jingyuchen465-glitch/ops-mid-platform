package com.bear.mcp.single.share.service;

import com.bear.mcp.single.common.exception.BusinessException;
import com.bear.mcp.single.core.entity.McpDataSourceEntity;
import com.bear.mcp.single.core.entity.McpDynamicToolEntity;
import com.bear.mcp.single.core.entity.McpRequestConfigEntity;
import com.bear.mcp.single.core.groovy.GroovyScriptEngine;
import com.bear.mcp.single.core.groovy.ScriptContext;
import com.bear.mcp.single.core.groovy.ScriptResult;
import com.bear.mcp.single.core.mapper.McpDataSourceMapper;
import com.bear.mcp.single.core.mapper.McpDynamicToolMapper;
import com.bear.mcp.single.core.mapper.McpRequestConfigMapper;
import com.bear.mcp.single.core.mapper.McpRoleMapper;
import com.bear.mcp.single.core.mapper.McpUserRoleMapper;
import com.bear.mcp.single.core.redis.RedisPermission;
import com.bear.mcp.single.core.redis.RedisPermissionPolicy;
import com.bear.mcp.single.share.req.ShareStudioToolDebugReq;
import com.bear.mcp.single.share.req.ShareStudioToolSaveReq;
import com.bear.mcp.single.share.res.ShareStudioToolDebugRes;
import com.bear.mcp.single.share.res.ShareStudioToolRes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 创作空间动态 Tool 服务。
 *
 * <p>API 创作负责把外部 HTTP 接口保存成 mcp_request_config；
 * Tool 创作负责把这些 API 配置包装成 AI Agent 可见的 MCP Tool。</p>
 */
@Service
public class ShareStudioToolService {

    /**
     * 草稿或下线状态。
     */
    private static final int STATUS_DRAFT = 0;

    /**
     * 已上线但不公开状态。
     */
    private static final int STATUS_PRIVATE = 1;

    /**
     * 已上线且公开状态。
     */
    private static final int STATUS_PUBLIC = 2;

    /**
     * mcp_dynamic_tool 表 Mapper。
     */
    private final McpDynamicToolMapper dynamicToolMapper;

    /**
     * mcp_request_config 表 Mapper，用于校验 API 白名单是否存在。
     */
    private final McpRequestConfigMapper requestConfigMapper;

    /**
     * mcp_data_source 表 Mapper，用于校验数据源白名单是否存在。
     */
    private final McpDataSourceMapper dataSourceMapper;

    /**
     * Groovy 脚本执行引擎。
     */
    private final GroovyScriptEngine groovyScriptEngine;

    /**
     * JSON 校验和转换工具。
     */
    private final ObjectMapper objectMapper;

    private final McpUserRoleMapper userRoleMapper;

    private final McpRoleMapper roleMapper;

    private final RedisPermissionPolicy redisPermissionPolicy;

    public ShareStudioToolService(McpDynamicToolMapper dynamicToolMapper,
                                  McpRequestConfigMapper requestConfigMapper,
                                  McpDataSourceMapper dataSourceMapper,
                                  GroovyScriptEngine groovyScriptEngine,
                                  ObjectMapper objectMapper,
                                  McpUserRoleMapper userRoleMapper,
                                  McpRoleMapper roleMapper,
                                  RedisPermissionPolicy redisPermissionPolicy) {
        this.dynamicToolMapper = dynamicToolMapper;
        this.requestConfigMapper = requestConfigMapper;
        this.dataSourceMapper = dataSourceMapper;
        this.groovyScriptEngine = groovyScriptEngine;
        this.objectMapper = objectMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.redisPermissionPolicy = redisPermissionPolicy;
    }

    /**
     * 查询创作空间中的动态 Tool。
     */
    public List<ShareStudioToolRes> list() {
        return dynamicToolMapper.findAll()
                .stream()
                .map(this::toRes)
                .toList();
    }

    /**
     * 新建动态 Tool。
     */
    @Transactional
    public ShareStudioToolRes create(Long currentUserId, ShareStudioToolSaveReq req) {
        if (dynamicToolMapper.findByName(req.getToolName()) != null) {
            throw new BusinessException(400, "工具名已存在，请换一个");
        }

        McpDynamicToolEntity entity = toEntity(req);
        List<RedisPermission> redisPermissions = normalize(entity);
        requireAdminIfRedisTool(currentUserId, redisPermissions);
        dynamicToolMapper.insert(entity);
        return toRes(entity);
    }

    /**
     * 更新动态 Tool。
     */
    @Transactional
    public ShareStudioToolRes update(Long currentUserId, Long id, ShareStudioToolSaveReq req) {
        McpDynamicToolEntity oldEntity = findById(id);
        McpDynamicToolEntity existsByName = dynamicToolMapper.findByName(req.getToolName());
        if (existsByName != null && !existsByName.getId().equals(id)) {
            throw new BusinessException(400, "工具名已存在，请换一个");
        }

        McpDynamicToolEntity entity = toEntity(req);
        entity.setId(oldEntity.getId());
        List<RedisPermission> redisPermissions = normalize(entity);
        requireAdminIfRedisTool(currentUserId, redisPermissions);
        requireAdminIfRedisTool(currentUserId, redisPermissionPolicy.parse(oldEntity.getLinkedRedisPermissions()));
        dynamicToolMapper.update(entity);
        return toRes(dynamicToolMapper.findById(id));
    }

    /**
     * 调试尚未保存的动态 Tool。
     */
    public ShareStudioToolDebugRes debugTemporary(Long currentUserId,
                                                  String currentUsername,
                                                  ShareStudioToolDebugReq req) {
        if (req.getTool() == null) {
            throw new BusinessException(400, "工具配置不能为空");
        }

        McpDynamicToolEntity entity = toEntity(req.getTool());
        List<RedisPermission> redisPermissions = normalize(entity);
        requireAdminIfRedisTool(currentUserId, redisPermissions);
        return executeDebug(currentUserId, currentUsername, entity, req.getParams());
    }

    /**
     * 调试已经保存的动态 Tool。
     */
    public ShareStudioToolDebugRes debug(Long currentUserId,
                                         String currentUsername,
                                         Long id,
                                         ShareStudioToolDebugReq req) {
        McpDynamicToolEntity entity = findById(id);
        List<RedisPermission> redisPermissions = normalize(entity);
        requireAdminIfRedisTool(currentUserId, redisPermissions);
        return executeDebug(currentUserId, currentUsername, entity, req.getParams());
    }

    /**
     * 公开发布动态 Tool。
     *
     * <p>状态 2 表示已进入 MCP 调用链路，并允许后续社区页面公开展示。</p>
     */
    @Transactional
    public ShareStudioToolRes publish(Long currentUserId, Long id) {
        McpDynamicToolEntity entity = findById(id);
        requireAdminIfRedisTool(currentUserId, redisPermissionPolicy.parse(entity.getLinkedRedisPermissions()));
        dynamicToolMapper.updatePublishStatus(id, 1, STATUS_PUBLIC);
        return toRes(dynamicToolMapper.findById(id));
    }

    /**
     * 不公开发布动态 Tool。
     *
     * <p>状态 1 表示已进入 MCP 调用链路，但只在自己的创作空间可见。</p>
     */
    @Transactional
    public ShareStudioToolRes publishPrivate(Long currentUserId, Long id) {
        McpDynamicToolEntity entity = findById(id);
        requireAdminIfRedisTool(currentUserId, redisPermissionPolicy.parse(entity.getLinkedRedisPermissions()));
        dynamicToolMapper.updatePublishStatus(id, 1, STATUS_PRIVATE);
        return toRes(dynamicToolMapper.findById(id));
    }

    /**
     * 下线动态 Tool。
     */
    @Transactional
    public ShareStudioToolRes unpublish(Long currentUserId, Long id) {
        McpDynamicToolEntity entity = findById(id);
        requireAdminIfRedisTool(currentUserId, redisPermissionPolicy.parse(entity.getLinkedRedisPermissions()));
        dynamicToolMapper.updatePublishStatus(id, 0, STATUS_DRAFT);
        return toRes(dynamicToolMapper.findById(id));
    }

    private ShareStudioToolDebugRes executeDebug(Long currentUserId,
                                                 String currentUsername,
                                                 McpDynamicToolEntity entity,
                                                 Map<String, Object> params) {
        ScriptResult result = groovyScriptEngine.execute(entity.getGroovyScript(), new ScriptContext(
                params,
                currentUserId,
                currentUsername,
                entity.getToolName(),
                parseStringList(entity.getLinkedRequestKeys()),
                parseLongList(entity.getLinkedDataSourceIds()),
                redisPermissionPolicy.parse(entity.getLinkedRedisPermissions()),
                30000
        ));

        ShareStudioToolDebugRes res = new ShareStudioToolDebugRes();
        res.setSuccess(result.success());
        res.setResult(result.result());
        res.setErrorMessage(result.errorMessage());
        res.setDurationMs(result.durationMs());
        return res;
    }

    private McpDynamicToolEntity findById(Long id) {
        McpDynamicToolEntity entity = dynamicToolMapper.findById(id);
        if (entity == null) {
            throw new BusinessException(404, "动态Tool不存在");
        }
        return entity;
    }

    private List<RedisPermission> normalize(McpDynamicToolEntity entity) {
        if (entity.getLinkedRequestKeys() == null || entity.getLinkedRequestKeys().isBlank()) {
            entity.setLinkedRequestKeys("[]");
        }
        if (entity.getLinkedDataSourceIds() == null || entity.getLinkedDataSourceIds().isBlank()) {
            entity.setLinkedDataSourceIds("[]");
        }
        if (entity.getLinkedRedisPermissions() == null || entity.getLinkedRedisPermissions().isBlank()) {
            entity.setLinkedRedisPermissions("[]");
        }
        if (entity.getEnabled() == null) {
            entity.setEnabled(0);
        }
        if (entity.getPublishStatus() == null) {
            entity.setPublishStatus(entity.getEnabled() != null && entity.getEnabled() == 1 ? STATUS_PRIVATE : STATUS_DRAFT);
        }
        if (entity.getPublishStatus() == STATUS_DRAFT) {
            entity.setEnabled(0);
        } else {
            entity.setEnabled(1);
        }

        validateJsonObject(entity.getInputSchema(), "入参Schema JSON");
        List<String> requestKeys = parseStringList(entity.getLinkedRequestKeys());
        validateLinkedRequestKeys(requestKeys);
        entity.setLinkedRequestKeys(toJson(requestKeys));
        List<Long> dataSourceIds = parseLongList(entity.getLinkedDataSourceIds());
        validateLinkedDataSourceIds(dataSourceIds);
        entity.setLinkedDataSourceIds(toJson(dataSourceIds));
        List<RedisPermission> redisPermissions = redisPermissionPolicy.parse(entity.getLinkedRedisPermissions());
        entity.setLinkedRedisPermissions(redisPermissionPolicy.toJson(redisPermissions));
        return redisPermissions;
    }

    private void validateJsonObject(String json, String fieldName) {
        try {
            objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new BusinessException(400, fieldName + "格式不正确");
        }
    }

    private List<String> parseStringList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new BusinessException(400, "API白名单JSON格式不正确");
        }
    }

    private List<Long> parseLongList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {
            });
        } catch (Exception e) {
            throw new BusinessException(400, "数据源白名单JSON格式不正确");
        }
    }

    private void validateLinkedRequestKeys(List<String> requestKeys) {
        for (String key : requestKeys) {
            McpRequestConfigEntity config = requestConfigMapper.findByConfigKey(key);
            if (config == null) {
                throw new BusinessException(400, "API配置不存在: " + key);
            }
            if (config.getPublishStatus() == null || config.getPublishStatus() == 0) {
                throw new BusinessException(400, "API配置尚未上线: " + key);
            }
        }
    }

    private void validateLinkedDataSourceIds(List<Long> dataSourceIds) {
        for (Long id : dataSourceIds) {
            if (id == null) {
                throw new BusinessException(400, "数据源ID不能为空");
            }
            McpDataSourceEntity dataSource = dataSourceMapper.findById(id);
            if (dataSource == null) {
                throw new BusinessException(400, "数据源不存在: " + id);
            }
            if (dataSource.getPublishStatus() == null || dataSource.getPublishStatus() == 0) {
                throw new BusinessException(400, "数据源尚未发布: " + id);
            }
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BusinessException(400, "JSON序列化失败");
        }
    }

    private McpDynamicToolEntity toEntity(ShareStudioToolSaveReq req) {
        McpDynamicToolEntity entity = new McpDynamicToolEntity();
        entity.setToolName(req.getToolName());
        entity.setToolDescription(req.getToolDescription());
        entity.setInputSchema(req.getInputSchema());
        entity.setGroovyScript(req.getGroovyScript());
        entity.setLinkedRequestKeys(req.getLinkedRequestKeys());
        entity.setLinkedDataSourceIds(req.getLinkedDataSourceIds());
        entity.setLinkedRedisPermissions(req.getLinkedRedisPermissions());
        entity.setEnabled(req.getEnabled());
        entity.setPublishStatus(req.getPublishStatus());
        return entity;
    }

    private ShareStudioToolRes toRes(McpDynamicToolEntity entity) {
        ShareStudioToolRes res = new ShareStudioToolRes();
        res.setId(entity.getId());
        res.setToolName(entity.getToolName());
        res.setToolDescription(entity.getToolDescription());
        res.setInputSchema(entity.getInputSchema());
        res.setGroovyScript(entity.getGroovyScript());
        res.setLinkedRequestKeys(entity.getLinkedRequestKeys());
        res.setLinkedDataSourceIds(entity.getLinkedDataSourceIds());
        res.setLinkedRedisPermissions(entity.getLinkedRedisPermissions());
        res.setEnabled(entity.getEnabled());
        res.setPublishStatus(entity.getPublishStatus());
        return res;
    }

    private void requireAdminIfRedisTool(Long currentUserId, List<RedisPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        boolean linkedToAdmin = currentUserId != null && userRoleMapper.findByUserId(currentUserId).stream()
                .anyMatch(role -> "ADMIN".equals(role.getRoleCode()));
        boolean admin = linkedToAdmin && !roleMapper.findEnabledByRoleCodes(List.of("ADMIN")).isEmpty();
        if (!admin) {
            throw new BusinessException(403, "只有管理员可以创建、调试或发布Redis型动态工具");
        }
    }
}
