package com.bear.mcp.single.share.service;

import com.bear.mcp.single.common.exception.BusinessException;
import com.bear.mcp.single.core.entity.McpRequestConfigEntity;
import com.bear.mcp.single.core.mapper.McpRequestConfigMapper;
import com.bear.mcp.single.core.request.RequestConfigService;
import com.bear.mcp.single.share.req.ShareStudioApiDebugReq;
import com.bear.mcp.single.share.req.ShareStudioApiSaveReq;
import com.bear.mcp.single.share.req.ShareStudioApiTemporaryDebugReq;
import com.bear.mcp.single.share.res.ShareStudioApiDebugRes;
import com.bear.mcp.single.share.res.ShareStudioApiRes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 创作空间的 HTTP API 服务。
 *
 * <p>share 包面向普通使用者的“创作空间”，它和 admin 包的治理后台分开：
 * admin 负责展示、审核、权限和运维；share 负责让用户把外部 HTTP API
 * 录入、调试并发布成后续可被动态工具引用的请求配置。</p>
 */
@Service
public class ShareStudioApiService {

    /**
     * 当前课程第 7 课只接入外部 HTTP API。
     */
    private static final String HTTP_TYPE = "HTTP";

    /**
     * API 草稿状态。
     */
    private static final int STATUS_DRAFT = 0;

    /**
     * API 上架但不公开状态。
     */
    private static final int STATUS_PRIVATE = 1;

    /**
     * API 公开上线状态。
     */
    private static final int STATUS_PUBLIC = 2;

    /**
     * 请求配置表 Mapper。
     */
    private final McpRequestConfigMapper requestConfigMapper;

    /**
     * 复用核心请求执行能力，调试时真正请求外部 HTTP API。
     */
    private final RequestConfigService requestConfigService;

    /**
     * 校验 JSON 字段是否是合法对象。
     */
    private final ObjectMapper objectMapper;

    public ShareStudioApiService(McpRequestConfigMapper requestConfigMapper,
                                 RequestConfigService requestConfigService,
                                 ObjectMapper objectMapper) {
        this.requestConfigMapper = requestConfigMapper;
        this.requestConfigService = requestConfigService;
        this.objectMapper = objectMapper;
    }

    /**
     * 查询当前用户创建的 API 配置。
     *
     * <p>创作空间强调“我的作品”，所以不会像管理后台一样返回所有人的配置。</p>
     */
    public List<ShareStudioApiRes> list(Long currentUserId) {
        return requestConfigMapper.findShareStudioApis(currentUserId)
                .stream()
                .map(this::toRes)
                .toList();
    }

    /**
     * 新建 HTTP API。
     *
     * <p>保存只是把配置落到 mcp_request_config，默认仍是草稿。
     * 用户后面需要主动点击上线或公开，才会改变发布状态。</p>
     */
    @Transactional
    public ShareStudioApiRes create(Long currentUserId, ShareStudioApiSaveReq req) {
        if (requestConfigMapper.findByConfigKey(req.getConfigKey()) != null) {
            throw new BusinessException(400, "配置Key已存在，请换一个");
        }

        McpRequestConfigEntity entity = toEntity(req);
        entity.setCreatorId(currentUserId);
        entity.setPublishStatus(STATUS_DRAFT);
        normalize(entity);

        requestConfigMapper.insert(entity);
        return toRes(entity);
    }

    /**
     * 更新 HTTP API。
     *
     * <p>更新配置时保留原来的创建者和发布状态，避免用户改一次 URL 就把
     * 草稿误发布，或者把已上线能力误下架。</p>
     */
    @Transactional
    public ShareStudioApiRes update(Long currentUserId, Long id, ShareStudioApiSaveReq req) {
        McpRequestConfigEntity oldEntity = findOwnApi(currentUserId, id);

        McpRequestConfigEntity existsByKey = requestConfigMapper.findByConfigKey(req.getConfigKey());
        if (existsByKey != null && !existsByKey.getId().equals(id)) {
            throw new BusinessException(400, "配置Key已存在，请换一个");
        }

        McpRequestConfigEntity entity = toEntity(req);
        entity.setId(id);
        entity.setCreatorId(oldEntity.getCreatorId() != null ? oldEntity.getCreatorId() : currentUserId);
        entity.setPublishStatus(oldEntity.getPublishStatus());
        normalize(entity);

        requestConfigMapper.update(entity);
        return toRes(requestConfigMapper.findById(id));
    }

    /**
     * 调试已经保存的 API。
     *
     * <p>配置来自数据库，本次请求只传调试参数。</p>
     */
    public ShareStudioApiDebugRes debug(Long currentUserId, Long id, ShareStudioApiDebugReq req) {
        McpRequestConfigEntity entity = findOwnApi(currentUserId, id);

        return executeDebug(entity, req.getParams());
    }

    /**
     * 调试尚未保存的 API。
     *
     * <p>前端点击“发送”时会把表单里的 URL、Header、Body、默认参数一起传过来。
     * 服务端临时组装 Entity 执行真实 HTTP 请求，但不会插入数据库。</p>
     */
    public ShareStudioApiDebugRes debugTemporary(Long currentUserId, ShareStudioApiTemporaryDebugReq req) {
        McpRequestConfigEntity entity = toEntity(req);
        entity.setCreatorId(currentUserId);
        entity.setPublishStatus(STATUS_DRAFT);
        normalize(entity);

        return executeDebug(entity, req.getParams());
    }

    /**
     * 执行调试并组装调试结果。
     *
     * <p>这里复用 core/request 里的请求执行能力，让课程代码保持分层：
     * share 负责创作空间流程，core 负责真正的 HTTP 调用。</p>
     */
    private ShareStudioApiDebugRes executeDebug(McpRequestConfigEntity entity, Map<String, Object> params) {
        long startedAt = System.currentTimeMillis();
        ShareStudioApiDebugRes res = new ShareStudioApiDebugRes();

        try {
            Object result = requestConfigService.executeTemporary(entity, params);
            res.setSuccess(true);
            res.setResult(result);
        } catch (Exception e) {
            res.setSuccess(false);
            res.setErrorMessage(e.getMessage());
        }

        res.setDurationMs(System.currentTimeMillis() - startedAt);
        return res;
    }

    /**
     * 公开发布 API。
     *
     * <p>状态 2 表示“已上线 + 公开”，后续社区页面可以展示它。</p>
     */
    @Transactional
    public ShareStudioApiRes publish(Long currentUserId, Long id) {
        McpRequestConfigEntity entity = findOwnApi(currentUserId, id);
        entity.setIsEnabled(1);
        entity.setPublishStatus(STATUS_PUBLIC);
        requestConfigMapper.update(entity);
        return toRes(requestConfigMapper.findById(id));
    }

    /**
     * 不公开发布 API。
     *
     * <p>状态 1 表示“已上线 + 不公开”，适合作者自己继续编排动态工具，
     * 但不出现在社区公开列表里。</p>
     */
    @Transactional
    public ShareStudioApiRes publishPrivate(Long currentUserId, Long id) {
        McpRequestConfigEntity entity = findOwnApi(currentUserId, id);
        entity.setIsEnabled(1);
        entity.setPublishStatus(STATUS_PRIVATE);
        requestConfigMapper.update(entity);
        return toRes(requestConfigMapper.findById(id));
    }

    /**
     * 下架 API。
     *
     * <p>状态回到 0，表示草稿。这里不删除记录，方便作者后续继续编辑。</p>
     */
    @Transactional
    public ShareStudioApiRes unpublish(Long currentUserId, Long id) {
        McpRequestConfigEntity entity = findOwnApi(currentUserId, id);
        entity.setPublishStatus(STATUS_DRAFT);
        requestConfigMapper.update(entity);
        return toRes(requestConfigMapper.findById(id));
    }

    /**
     * 查找当前用户自己的 HTTP API。
     *
     * <p>这是创作空间的安全边界：用户只能操作自己创建的 API，
     * 并且当前课程只允许操作 HTTP 类型配置。</p>
     */
    private McpRequestConfigEntity findOwnApi(Long currentUserId, Long id) {
        McpRequestConfigEntity entity = requestConfigMapper.findById(id);
        if (entity == null || !HTTP_TYPE.equalsIgnoreCase(entity.getType())) {
            throw new BusinessException(404, "API配置不存在");
        }

        if (entity.getCreatorId() != null && !entity.getCreatorId().equals(currentUserId)) {
            throw new BusinessException(403, "不能操作其他用户创建的API");
        }

        return entity;
    }

    /**
     * 统一整理 HTTP API 配置的默认值。
     *
     * <p>前端可以少传一些非核心字段，后端在这里补齐默认值，
     * 保证入库数据形态稳定，也保证 Entity 和表字段一一对应。</p>
     */
    private void normalize(McpRequestConfigEntity entity) {
        entity.setType(HTTP_TYPE);
        entity.setMethod(entity.getMethod() != null ? entity.getMethod().toUpperCase() : "GET");
        entity.setServiceName(null);
        entity.setMethodName(null);
        entity.setArgsSchema("{}");

        if (entity.getHeaders() == null || entity.getHeaders().isBlank()) {
            entity.setHeaders("{}");
        }
        if (entity.getParamsDefault() == null || entity.getParamsDefault().isBlank()) {
            entity.setParamsDefault("{}");
        }
        if (entity.getBodyTemplate() == null) {
            entity.setBodyTemplate("");
        }
        if (entity.getConnectTimeoutMs() == null) {
            entity.setConnectTimeoutMs(5000);
        }
        if (entity.getReadTimeoutMs() == null) {
            entity.setReadTimeoutMs(15000);
        }
        if (entity.getRateLimitPerMinute() == null) {
            entity.setRateLimitPerMinute(0);
        }
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(1);
        }

        validateJsonObject(entity.getHeaders(), "请求头JSON");
        validateJsonObject(entity.getParamsDefault(), "默认参数JSON");
    }

    /**
     * 校验字符串是否是 JSON 对象。
     *
     * <p>Header 和默认参数后续都要按 Map 使用，所以这里不接受数组或普通字符串。</p>
     */
    private void validateJsonObject(String json, String fieldName) {
        try {
            objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new BusinessException(400, fieldName + "格式不正确");
        }
    }

    /**
     * Req 转 Entity。
     *
     * <p>Controller 不直接接收 Entity，转换逻辑放在 Service，方便控制哪些字段
     * 可以由前端传入，哪些字段只能由后端决定。</p>
     */
    private McpRequestConfigEntity toEntity(ShareStudioApiSaveReq req) {
        McpRequestConfigEntity entity = new McpRequestConfigEntity();
        entity.setRequestId(req.getRequestId());
        entity.setConfigKey(req.getConfigKey());
        entity.setName(req.getName());
        entity.setType(req.getType());
        entity.setMethod(req.getMethod());
        entity.setUrl(req.getUrl());
        entity.setHeaders(req.getHeaders());
        entity.setBodyTemplate(req.getBodyTemplate());
        entity.setParamsDefault(req.getParamsDefault());
        entity.setConnectTimeoutMs(req.getConnectTimeoutMs());
        entity.setReadTimeoutMs(req.getReadTimeoutMs());
        entity.setIsEnabled(req.getIsEnabled());
        entity.setRateLimitPerMinute(req.getRateLimitPerMinute());
        entity.setDescription(req.getDescription());
        entity.setCategory(req.getCategory());
        return entity;
    }

    /**
     * Entity 转 Res。
     *
     * <p>返回给前端的对象和数据库 Entity 分开，避免把内部实现细节泄漏给页面。</p>
     */
    private ShareStudioApiRes toRes(McpRequestConfigEntity entity) {
        ShareStudioApiRes res = new ShareStudioApiRes();
        res.setId(entity.getId());
        res.setRequestId(entity.getRequestId());
        res.setConfigKey(entity.getConfigKey());
        res.setName(entity.getName());
        res.setType(entity.getType());
        res.setMethod(entity.getMethod());
        res.setUrl(entity.getUrl());
        res.setHeaders(entity.getHeaders());
        res.setBodyTemplate(entity.getBodyTemplate());
        res.setParamsDefault(entity.getParamsDefault());
        res.setConnectTimeoutMs(entity.getConnectTimeoutMs());
        res.setReadTimeoutMs(entity.getReadTimeoutMs());
        res.setCreatorId(entity.getCreatorId());
        res.setIsEnabled(entity.getIsEnabled());
        res.setRateLimitPerMinute(entity.getRateLimitPerMinute());
        res.setPublishStatus(entity.getPublishStatus());
        res.setDescription(entity.getDescription());
        res.setCategory(entity.getCategory());
        res.setCreateTime(entity.getCreateTime());
        res.setUpdateTime(entity.getUpdateTime());
        return res;
    }
}
