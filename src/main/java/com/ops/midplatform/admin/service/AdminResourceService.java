package com.ops.midplatform.admin.service;

import com.ops.midplatform.admin.res.AdminResourceRes;
import com.ops.midplatform.common.exception.BusinessException;
import com.ops.midplatform.core.entity.McpResourceEntity;
import com.ops.midplatform.core.mapper.McpResourceMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** 后台 Resource 选项服务。 */
@Service
public class AdminResourceService {
    private final McpResourceMapper resourceMapper;

    public AdminResourceService(McpResourceMapper resourceMapper) {
        this.resourceMapper = resourceMapper;
    }

    public List<AdminResourceRes> list() {
        return resourceMapper.findAll().stream().map(this::toRes).toList();
    }

    public AdminResourceRes enable(Long id) {
        return updateEnabled(id, 1);
    }

    public AdminResourceRes disable(Long id) {
        return updateEnabled(id, 0);
    }

    private AdminResourceRes updateEnabled(Long id, Integer enabled) {
        McpResourceEntity entity = resourceMapper.findById(id);
        if (entity == null) {
            throw new BusinessException(404, "Resource不存在");
        }
        resourceMapper.updateEnabled(id, enabled);
        return toRes(resourceMapper.findById(id));
    }

    private AdminResourceRes toRes(McpResourceEntity entity) {
        AdminResourceRes res = new AdminResourceRes();
        res.setId(entity.getId());
        res.setResourceUri(entity.getResourceUri());
        res.setName(entity.getName());
        res.setDescription(entity.getDescription());
        res.setMimeType(entity.getMimeType());
        res.setEnabled(entity.getEnabled());
        res.setPublishStatus(entity.getPublishStatus());
        return res;
    }
}
