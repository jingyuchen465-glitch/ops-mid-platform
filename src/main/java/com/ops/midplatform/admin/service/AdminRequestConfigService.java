package com.bear.mcp.single.admin.service;

import com.bear.mcp.single.admin.req.AdminRequestConfigSaveReq;
import com.bear.mcp.single.admin.res.AdminRequestConfigRes;
import com.bear.mcp.single.core.entity.McpRequestConfigEntity;
import com.bear.mcp.single.core.mapper.McpRequestConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminRequestConfigService {
    private final McpRequestConfigMapper mapper;

    public AdminRequestConfigService(McpRequestConfigMapper mapper) {
        this.mapper = mapper;
    }

    public List<AdminRequestConfigRes> list() {
        return mapper.findAll().stream().map(this::toRes).toList();
    }

    public AdminRequestConfigRes create(AdminRequestConfigSaveReq req) {
        McpRequestConfigEntity entity = toEntity(req);
        normalize(entity);
        mapper.insert(entity);
        return toRes(entity);
    }

    public AdminRequestConfigRes update(Long id, AdminRequestConfigSaveReq req) {
        McpRequestConfigEntity entity = toEntity(req);
        entity.setId(id);
        normalize(entity);
        mapper.update(entity);
        return toRes(entity);
    }

    private void normalize(McpRequestConfigEntity entity) {
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(1);
        }
        if (entity.getPublishStatus() == null) {
            entity.setPublishStatus(0);
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
        if (entity.getHeaders() == null) {
            entity.setHeaders("{}");
        }
        if (entity.getParamsDefault() == null) {
            entity.setParamsDefault("{}");
        }
    }

    private McpRequestConfigEntity toEntity(AdminRequestConfigSaveReq req) {
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
        entity.setServiceName(req.getServiceName());
        entity.setMethodName(req.getMethodName());
        entity.setArgsSchema(req.getArgsSchema());
        entity.setCreatorId(req.getCreatorId());
        entity.setIsEnabled(req.getIsEnabled());
        entity.setRateLimitPerMinute(req.getRateLimitPerMinute());
        entity.setPublishStatus(req.getPublishStatus());
        entity.setDescription(req.getDescription());
        entity.setCategory(req.getCategory());
        return entity;
    }

    private AdminRequestConfigRes toRes(McpRequestConfigEntity entity) {
        AdminRequestConfigRes res = new AdminRequestConfigRes();
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
        res.setServiceName(entity.getServiceName());
        res.setMethodName(entity.getMethodName());
        res.setArgsSchema(entity.getArgsSchema());
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
