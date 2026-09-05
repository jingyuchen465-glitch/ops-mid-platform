package com.bear.mcp.single.share.service;

import com.bear.mcp.single.common.exception.BusinessException;
import com.bear.mcp.single.core.entity.McpResourceEntity;
import com.bear.mcp.single.core.mapper.McpResourceMapper;
import com.bear.mcp.single.core.storage.TosStorageService;
import com.bear.mcp.single.share.req.ShareStudioResourcePresignReq;
import com.bear.mcp.single.share.req.ShareStudioResourceSaveReq;
import com.bear.mcp.single.share.res.ShareStudioResourcePresignRes;
import com.bear.mcp.single.share.res.ShareStudioResourcePresignDownloadRes;
import com.bear.mcp.single.share.res.ShareStudioResourceRes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

/** 创作空间 Resource 服务。 */
@Service
public class ShareStudioResourceService {
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PRIVATE = 1;
    private static final int STATUS_PUBLIC = 2;
    private static final String MIME_MARKDOWN = "text/markdown";
    private static final long MAX_MD_FILE_SIZE = 2 * 1024 * 1024L;

    private final McpResourceMapper resourceMapper;
    private final TosStorageService tosStorageService;

    public ShareStudioResourceService(McpResourceMapper resourceMapper,
                                      TosStorageService tosStorageService) {
        this.resourceMapper = resourceMapper;
        this.tosStorageService = tosStorageService;
    }

    public List<ShareStudioResourceRes> list(Long creatorId) {
        return resourceMapper.findShareStudioResources(creatorId).stream().map(this::toRes).toList();
    }

    public ShareStudioResourcePresignRes presignUpload(Long creatorId, ShareStudioResourcePresignReq req) {
        String uri = normalizeUri(req.getResourceUri());
        validateMdFile(req.getFileName(), req.getFileSize());
        String objectKey = buildObjectKey(creatorId, uri);
        TosStorageService.PresignResult result = tosStorageService.presignPut(objectKey);

        ShareStudioResourcePresignRes res = new ShareStudioResourcePresignRes();
        res.setUploadUrl(result.uploadUrl());
        res.setObjectKey(result.objectKey());
        res.setExpires(result.expires());
        return res;
    }

    public ShareStudioResourcePresignDownloadRes presignDownload(Long creatorId, Long id) {
        McpResourceEntity entity = findById(id);
        checkOwner(creatorId, entity);
        TosStorageService.PresignResult result = tosStorageService.presignGet(entity.getObjectKey());

        ShareStudioResourcePresignDownloadRes res = new ShareStudioResourcePresignDownloadRes();
        res.setDownloadUrl(result.uploadUrl());
        res.setExpires(result.expires());
        return res;
    }

    @Transactional
    public ShareStudioResourceRes create(Long creatorId, ShareStudioResourceSaveReq req) {
        if (resourceMapper.findByUri(req.getResourceUri()) != null) {
            throw new BusinessException(400, "Resource URI已存在，请换一个");
        }
        McpResourceEntity entity = toEntity(req);
        entity.setCreatorId(creatorId);
        normalize(entity);
        validateObjectKey(creatorId, entity.getObjectKey());
        resourceMapper.insert(entity);
        return toRes(entity);
    }

    @Transactional
    public ShareStudioResourceRes update(Long creatorId, Long id, ShareStudioResourceSaveReq req) {
        McpResourceEntity oldEntity = findById(id);
        checkOwner(creatorId, oldEntity);
        McpResourceEntity existsByUri = resourceMapper.findByUri(req.getResourceUri());
        if (existsByUri != null && !existsByUri.getId().equals(id)) {
            throw new BusinessException(400, "Resource URI已存在，请换一个");
        }
        McpResourceEntity entity = toEntity(req);
        entity.setId(id);
        entity.setCreatorId(oldEntity.getCreatorId());
        normalize(entity);
        validateObjectKey(creatorId, entity.getObjectKey());
        resourceMapper.update(entity);
        return toRes(resourceMapper.findById(id));
    }

    @Transactional
    public ShareStudioResourceRes publish(Long creatorId, Long id) {
        McpResourceEntity entity = findById(id);
        checkOwner(creatorId, entity);
        resourceMapper.updatePublishStatus(id, 1, STATUS_PUBLIC);
        return toRes(resourceMapper.findById(id));
    }

    @Transactional
    public ShareStudioResourceRes publishPrivate(Long creatorId, Long id) {
        McpResourceEntity entity = findById(id);
        checkOwner(creatorId, entity);
        resourceMapper.updatePublishStatus(id, 1, STATUS_PRIVATE);
        return toRes(resourceMapper.findById(id));
    }

    @Transactional
    public ShareStudioResourceRes unpublish(Long creatorId, Long id) {
        McpResourceEntity entity = findById(id);
        checkOwner(creatorId, entity);
        resourceMapper.updatePublishStatus(id, 0, STATUS_DRAFT);
        return toRes(resourceMapper.findById(id));
    }

    private void normalize(McpResourceEntity entity) {
        entity.setResourceUri(normalizeUri(entity.getResourceUri()));
        entity.setMimeType(MIME_MARKDOWN);
        if (entity.getDescription() == null) {
            entity.setDescription("");
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

    private String normalizeUri(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, "Resource URI不能为空");
        }
        String uri = value.trim();
        if (!uri.matches("^[a-zA-Z][a-zA-Z0-9_:/.-]{1,254}$")) {
            throw new BusinessException(400, "Resource URI只能包含字母、数字、下划线、中划线、点、冒号和斜杠，并以字母开头");
        }
        return uri;
    }

    private void validateMdFile(String fileName, Long fileSize) {
        if (StringUtils.hasText(fileName) && !fileName.toLowerCase(Locale.ROOT).endsWith(".md")) {
            throw new BusinessException(400, "当前 Resource 只支持上传 Markdown (.md) 文件");
        }
        if (fileSize != null && fileSize > MAX_MD_FILE_SIZE) {
            throw new BusinessException(400, "Markdown 文件不能超过 2MB");
        }
    }

    private String buildObjectKey(Long creatorId, String resourceUri) {
        String key = resourceUri.replaceFirst("^[a-zA-Z][a-zA-Z0-9+.-]*://", "");
        key = key.replace('\\', '/').replaceAll("[^a-zA-Z0-9_./-]", "_").replaceAll("/+", "/");
        while (key.startsWith("/")) {
            key = key.substring(1);
        }
        if (!key.toLowerCase(Locale.ROOT).endsWith(".md")) {
            key = key + ".md";
        }
        return "resource/" + creatorId + "/" + key;
    }

    private void validateObjectKey(Long creatorId, String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new BusinessException(400, "请先上传 Markdown 文件");
        }
        String prefix = "resource/" + creatorId + "/";
        if (!objectKey.startsWith(prefix) || objectKey.contains("..") || objectKey.startsWith("/") || !objectKey.endsWith(".md")) {
            throw new BusinessException(400, "Resource 文件地址不合法");
        }
    }

    private McpResourceEntity findById(Long id) {
        McpResourceEntity entity = resourceMapper.findById(id);
        if (entity == null) {
            throw new BusinessException(404, "Resource不存在");
        }
        return entity;
    }

    private void checkOwner(Long creatorId, McpResourceEntity entity) {
        if (entity.getCreatorId() != null && !entity.getCreatorId().equals(creatorId)) {
            throw new BusinessException(403, "不能修改其他用户创建的 Resource");
        }
    }

    private McpResourceEntity toEntity(ShareStudioResourceSaveReq req) {
        McpResourceEntity entity = new McpResourceEntity();
        entity.setResourceUri(req.getResourceUri());
        entity.setName(req.getName());
        entity.setDescription(req.getDescription());
        entity.setMimeType(req.getMimeType());
        entity.setObjectKey(req.getObjectKey());
        entity.setFileName(req.getFileName());
        entity.setFileSize(req.getFileSize());
        entity.setEnabled(req.getEnabled());
        entity.setPublishStatus(req.getPublishStatus());
        return entity;
    }

    private ShareStudioResourceRes toRes(McpResourceEntity entity) {
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
        return res;
    }
}
