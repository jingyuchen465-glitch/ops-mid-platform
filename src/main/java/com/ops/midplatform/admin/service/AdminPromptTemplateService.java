package com.ops.midplatform.admin.service;

import com.ops.midplatform.admin.res.AdminPromptTemplateRes;
import com.ops.midplatform.core.entity.McpPromptTemplateEntity;
import com.ops.midplatform.core.mapper.McpPromptTemplateMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** 管理后台 Prompt 权限配置需要的 Prompt 选项。 */
@Service
public class AdminPromptTemplateService {
    private final McpPromptTemplateMapper promptTemplateMapper;

    public AdminPromptTemplateService(McpPromptTemplateMapper promptTemplateMapper) {
        this.promptTemplateMapper = promptTemplateMapper;
    }

    public List<AdminPromptTemplateRes> listOptions() {
        return promptTemplateMapper.findAll().stream().map(this::toRes).toList();
    }

    private AdminPromptTemplateRes toRes(McpPromptTemplateEntity entity) {
        AdminPromptTemplateRes res = new AdminPromptTemplateRes();
        res.setId(entity.getId());
        res.setPromptName(entity.getPromptName());
        res.setTitle(entity.getTitle());
        res.setDescription(entity.getDescription());
        res.setEnabled(entity.getEnabled());
        res.setPublishStatus(entity.getPublishStatus());
        return res;
    }
}
