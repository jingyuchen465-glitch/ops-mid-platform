package com.ops.midplatform.share.service;

import com.ops.midplatform.common.exception.BusinessException;
import com.ops.midplatform.core.entity.McpPromptTemplateEntity;
import com.ops.midplatform.core.mapper.McpPromptTemplateMapper;
import com.ops.midplatform.core.prompt.PromptTemplateService;
import com.ops.midplatform.share.req.ShareStudioPromptDebugReq;
import com.ops.midplatform.share.req.ShareStudioPromptSaveReq;
import com.ops.midplatform.share.res.ShareStudioPromptDebugRes;
import com.ops.midplatform.share.res.ShareStudioPromptRes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/** 创作空间 Prompt 模板服务。 */
@Service
public class ShareStudioPromptService {
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PRIVATE = 1;
    private static final int STATUS_PUBLIC = 2;

    private final McpPromptTemplateMapper promptMapper;
    private final PromptTemplateService promptTemplateService;

    public ShareStudioPromptService(McpPromptTemplateMapper promptMapper,
                                    PromptTemplateService promptTemplateService) {
        this.promptMapper = promptMapper;
        this.promptTemplateService = promptTemplateService;
    }

    public List<ShareStudioPromptRes> list(Long creatorId) {
        return promptMapper.findShareStudioPrompts(creatorId)
                .stream()
                .map(this::toRes)
                .toList();
    }

    @Transactional
    public ShareStudioPromptRes create(Long creatorId, ShareStudioPromptSaveReq req) {
        if (promptMapper.findByName(req.getPromptName()) != null) {
            throw new BusinessException(400, "Prompt名称已存在，请换一个");
        }
        McpPromptTemplateEntity entity = toEntity(req);
        entity.setCreatorId(creatorId);
        normalize(entity);
        promptMapper.insert(entity);
        return toRes(entity);
    }

    @Transactional
    public ShareStudioPromptRes update(Long creatorId, Long id, ShareStudioPromptSaveReq req) {
        McpPromptTemplateEntity oldEntity = findById(id);
        checkOwner(creatorId, oldEntity);
        McpPromptTemplateEntity existsByName = promptMapper.findByName(req.getPromptName());
        if (existsByName != null && !existsByName.getId().equals(id)) {
            throw new BusinessException(400, "Prompt名称已存在，请换一个");
        }
        McpPromptTemplateEntity entity = toEntity(req);
        entity.setId(id);
        entity.setCreatorId(oldEntity.getCreatorId());
        normalize(entity);
        promptMapper.update(entity);
        return toRes(promptMapper.findById(id));
    }

    public ShareStudioPromptDebugRes debugTemporary(ShareStudioPromptDebugReq req) {
        if (req.getPrompt() == null) {
            throw new BusinessException(400, "Prompt配置不能为空");
        }
        McpPromptTemplateEntity entity = toEntity(req.getPrompt());
        normalize(entity);
        return render(entity, req.getArguments());
    }

    public ShareStudioPromptDebugRes debug(Long creatorId, Long id, ShareStudioPromptDebugReq req) {
        McpPromptTemplateEntity entity = findById(id);
        checkOwner(creatorId, entity);
        return render(entity, req.getArguments());
    }

    @Transactional
    public ShareStudioPromptRes publish(Long creatorId, Long id) {
        McpPromptTemplateEntity entity = findById(id);
        checkOwner(creatorId, entity);
        promptMapper.updatePublishStatus(id, 1, STATUS_PUBLIC);
        return toRes(promptMapper.findById(id));
    }

    @Transactional
    public ShareStudioPromptRes publishPrivate(Long creatorId, Long id) {
        McpPromptTemplateEntity entity = findById(id);
        checkOwner(creatorId, entity);
        promptMapper.updatePublishStatus(id, 1, STATUS_PRIVATE);
        return toRes(promptMapper.findById(id));
    }

    @Transactional
    public ShareStudioPromptRes unpublish(Long creatorId, Long id) {
        McpPromptTemplateEntity entity = findById(id);
        checkOwner(creatorId, entity);
        promptMapper.updatePublishStatus(id, 0, STATUS_DRAFT);
        return toRes(promptMapper.findById(id));
    }

    private ShareStudioPromptDebugRes render(McpPromptTemplateEntity entity, Map<String, Object> arguments) {
        ShareStudioPromptDebugRes res = new ShareStudioPromptDebugRes();
        res.setSuccess(true);
        res.setArguments(arguments != null ? arguments : Map.of());
        res.setRenderedContent(promptTemplateService.render(entity.getTemplateContent(), arguments));
        return res;
    }

    private McpPromptTemplateEntity findById(Long id) {
        McpPromptTemplateEntity entity = promptMapper.findById(id);
        if (entity == null) {
            throw new BusinessException(404, "Prompt模板不存在");
        }
        return entity;
    }

    private void checkOwner(Long creatorId, McpPromptTemplateEntity entity) {
        if (entity.getCreatorId() != null && !entity.getCreatorId().equals(creatorId)) {
            throw new BusinessException(403, "不能修改其他用户创建的 Prompt");
        }
    }

    private void normalize(McpPromptTemplateEntity entity) {
        entity.setPromptName(normalizeName(entity.getPromptName()));
        if (entity.getDescription() == null) {
            entity.setDescription("");
        }
        entity.setArgumentsSchema(promptTemplateService.normalizeArgumentsSchema(entity.getArgumentsSchema()));
        entity.setLinkedToolNames(promptTemplateService.normalizeToolNames(entity.getLinkedToolNames()));
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
    }

    private String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(400, "Prompt名称不能为空");
        }
        String name = value.trim();
        if (!name.matches("^[a-zA-Z][a-zA-Z0-9_]{1,127}$")) {
            throw new BusinessException(400, "Prompt名称只能包含字母、数字、下划线，并以字母开头");
        }
        return name;
    }

    private McpPromptTemplateEntity toEntity(ShareStudioPromptSaveReq req) {
        McpPromptTemplateEntity entity = new McpPromptTemplateEntity();
        entity.setPromptName(req.getPromptName());
        entity.setTitle(req.getTitle());
        entity.setDescription(req.getDescription());
        entity.setArgumentsSchema(req.getArgumentsSchema());
        entity.setTemplateContent(req.getTemplateContent());
        entity.setLinkedToolNames(req.getLinkedToolNames());
        entity.setEnabled(req.getEnabled());
        entity.setPublishStatus(req.getPublishStatus());
        return entity;
    }

    private ShareStudioPromptRes toRes(McpPromptTemplateEntity entity) {
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
        return res;
    }
}
