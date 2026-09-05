package com.ops.midplatform.admin.service;

import com.ops.midplatform.admin.res.AdminDynamicToolRes;
import com.ops.midplatform.core.entity.McpDynamicToolEntity;
import com.ops.midplatform.core.mapper.McpDynamicToolMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** 管理端只读查看已发布动态工具；创作和编辑属于创作空间。 */
@Service
public class AdminDynamicToolService {
    private final McpDynamicToolMapper mapper;

    public AdminDynamicToolService(McpDynamicToolMapper mapper) {
        this.mapper = mapper;
    }

    public List<AdminDynamicToolRes> list() {
        return mapper.findAll().stream().map(this::toRes).toList();
    }

    private AdminDynamicToolRes toRes(McpDynamicToolEntity entity) {
        AdminDynamicToolRes res = new AdminDynamicToolRes();
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
}
