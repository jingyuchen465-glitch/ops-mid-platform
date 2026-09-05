package com.bear.mcp.single.share.service;

import com.bear.mcp.single.common.exception.BusinessException;
import com.bear.mcp.single.core.entity.McpSkillEntity;
import com.bear.mcp.single.core.mapper.McpSkillMapper;
import com.bear.mcp.single.core.storage.TosStorageService;
import com.bear.mcp.single.share.req.ShareStudioSkillPresignReq;
import com.bear.mcp.single.share.req.ShareStudioSkillSaveReq;
import com.bear.mcp.single.share.res.ShareStudioSkillPresignDownloadRes;
import com.bear.mcp.single.share.res.ShareStudioSkillPresignRes;
import com.bear.mcp.single.share.res.ShareStudioSkillRes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

/** 创作空间 Skill 服务。 */
@Service
public class ShareStudioSkillService {
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PRIVATE = 1;
    private static final int STATUS_PUBLIC = 2;
    private static final long MAX_MD_FILE_SIZE = 2 * 1024 * 1024L;

    private final McpSkillMapper skillMapper;
    private final TosStorageService tosStorageService;

    public ShareStudioSkillService(McpSkillMapper skillMapper,
                                   TosStorageService tosStorageService) {
        this.skillMapper = skillMapper;
        this.tosStorageService = tosStorageService;
    }

    public List<ShareStudioSkillRes> list(Long creatorId) {
        return skillMapper.findShareStudioSkills(creatorId).stream().map(this::toRes).toList();
    }

    public ShareStudioSkillPresignRes presignUpload(Long creatorId, ShareStudioSkillPresignReq req) {
        validateMdFile(req.getFileName(), req.getFileSize());
        String skillCode = StringUtils.hasText(req.getSkillCode()) ? normalizeSkillCode(req.getSkillCode()) : nextSkillCode();
        String objectKey = "skills/" + skillCode + "/SKILL.md";
        TosStorageService.PresignResult result = tosStorageService.presignPut(objectKey);

        ShareStudioSkillPresignRes res = new ShareStudioSkillPresignRes();
        res.setUploadUrl(result.uploadUrl());
        res.setObjectKey(result.objectKey());
        res.setSkillCode(skillCode);
        res.setExpires(result.expires());
        return res;
    }

    public ShareStudioSkillPresignDownloadRes presignDownload(Long creatorId, Long id) {
        McpSkillEntity entity = findById(id);
        checkOwner(creatorId, entity);
        TosStorageService.PresignResult result = tosStorageService.presignGet(entity.getObjectKey());

        ShareStudioSkillPresignDownloadRes res = new ShareStudioSkillPresignDownloadRes();
        res.setDownloadUrl(result.uploadUrl());
        res.setExpires(result.expires());
        return res;
    }

    @Transactional
    public ShareStudioSkillRes create(Long creatorId, ShareStudioSkillSaveReq req) {
        McpSkillEntity entity = toEntity(req);
        entity.setCreatorId(creatorId);
        normalize(entity);
        if (skillMapper.findByCode(entity.getSkillCode()) != null) {
            throw new BusinessException(400, "Skill ID已存在，请重新上传生成");
        }
        validateObjectKey(entity.getSkillCode(), entity.getObjectKey());
        skillMapper.insert(entity);
        return toRes(entity);
    }

    @Transactional
    public ShareStudioSkillRes update(Long creatorId, Long id, ShareStudioSkillSaveReq req) {
        McpSkillEntity oldEntity = findById(id);
        checkOwner(creatorId, oldEntity);
        McpSkillEntity entity = toEntity(req);
        entity.setId(id);
        entity.setCreatorId(oldEntity.getCreatorId());
        normalize(entity);
        if (!oldEntity.getSkillCode().equals(entity.getSkillCode())) {
            throw new BusinessException(400, "已创建的 Skill ID 不允许修改");
        }
        validateObjectKey(entity.getSkillCode(), entity.getObjectKey());
        skillMapper.update(entity);
        return toRes(skillMapper.findById(id));
    }

    @Transactional
    public ShareStudioSkillRes publish(Long creatorId, Long id) {
        McpSkillEntity entity = findById(id);
        checkOwner(creatorId, entity);
        skillMapper.updatePublishStatus(id, 1, STATUS_PUBLIC);
        return toRes(skillMapper.findById(id));
    }

    @Transactional
    public ShareStudioSkillRes publishPrivate(Long creatorId, Long id) {
        McpSkillEntity entity = findById(id);
        checkOwner(creatorId, entity);
        skillMapper.updatePublishStatus(id, 1, STATUS_PRIVATE);
        return toRes(skillMapper.findById(id));
    }

    @Transactional
    public ShareStudioSkillRes unpublish(Long creatorId, Long id) {
        McpSkillEntity entity = findById(id);
        checkOwner(creatorId, entity);
        skillMapper.updatePublishStatus(id, 0, STATUS_DRAFT);
        return toRes(skillMapper.findById(id));
    }

    private void normalize(McpSkillEntity entity) {
        entity.setSkillCode(normalizeSkillCode(entity.getSkillCode()));
        entity.setName(normalizeName(entity.getName()));
        if (entity.getDescription() == null) {
            entity.setDescription("");
        }
        if (entity.getCategory() == null) {
            entity.setCategory("");
        }
        validateMdFile(entity.getFileName(), entity.getFileSize());
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

    private String nextSkillCode() {
        String max = skillMapper.findMaxSkillCode();
        long next = 1L;
        if (StringUtils.hasText(max)) {
            next = Long.parseLong(max.substring("SKILL".length())) + 1;
        }
        return "SKILL" + String.format("%010d", next);
    }

    private String normalizeSkillCode(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, "Skill ID不能为空");
        }
        String skillCode = value.trim().toUpperCase(Locale.ROOT);
        if (!skillCode.matches("^SKILL[0-9]{10}$")) {
            throw new BusinessException(400, "Skill ID格式必须为 SKILL+10位数字");
        }
        return skillCode;
    }

    private String normalizeName(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, "Skill名称不能为空");
        }
        return value.trim();
    }

    private void validateMdFile(String fileName, Long fileSize) {
        if (StringUtils.hasText(fileName) && !fileName.toLowerCase(Locale.ROOT).endsWith(".md")) {
            throw new BusinessException(400, "当前 Skill 只支持上传 Markdown (.md) 文件");
        }
        if (fileSize != null && fileSize > MAX_MD_FILE_SIZE) {
            throw new BusinessException(400, "Markdown 文件不能超过 2MB");
        }
    }

    private void validateObjectKey(String skillCode, String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new BusinessException(400, "请先上传 Markdown 文件");
        }
        String expected = "skills/" + skillCode + "/SKILL.md";
        if (!expected.equals(objectKey)) {
            throw new BusinessException(400, "Skill 文件地址不合法");
        }
    }

    private McpSkillEntity findById(Long id) {
        McpSkillEntity entity = skillMapper.findById(id);
        if (entity == null) {
            throw new BusinessException(404, "Skill不存在");
        }
        return entity;
    }

    private void checkOwner(Long creatorId, McpSkillEntity entity) {
        if (entity.getCreatorId() != null && !entity.getCreatorId().equals(creatorId)) {
            throw new BusinessException(403, "不能修改其他用户创建的 Skill");
        }
    }

    private McpSkillEntity toEntity(ShareStudioSkillSaveReq req) {
        McpSkillEntity entity = new McpSkillEntity();
        entity.setSkillCode(req.getSkillCode());
        entity.setName(req.getName());
        entity.setDescription(req.getDescription());
        entity.setCategory(req.getCategory());
        entity.setObjectKey(req.getObjectKey());
        entity.setFileName(req.getFileName());
        entity.setFileSize(req.getFileSize());
        entity.setEnabled(req.getEnabled());
        entity.setPublishStatus(req.getPublishStatus());
        return entity;
    }

    private ShareStudioSkillRes toRes(McpSkillEntity entity) {
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
        return res;
    }
}
