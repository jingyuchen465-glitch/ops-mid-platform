package com.ops.midplatform.core.resource;

import com.ops.midplatform.core.context.McpUserContext;
import com.ops.midplatform.core.entity.McpResourceEntity;
import com.ops.midplatform.core.entity.McpUserResourceSelectionEntity;
import com.ops.midplatform.core.mapper.McpResourceMapper;
import com.ops.midplatform.core.mapper.McpUserResourceSelectionMapper;
import com.ops.midplatform.core.storage.TosStorageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** MCP Resource 运行时权限和内容读取。 */
@Service
public class ResourceAccessService {
    private final McpResourceMapper resourceMapper;
    private final McpUserResourceSelectionMapper resourceSelectionMapper;
    private final TosStorageService tosStorageService;

    public ResourceAccessService(McpResourceMapper resourceMapper,
                                 McpUserResourceSelectionMapper resourceSelectionMapper,
                                 TosStorageService tosStorageService) {
        this.resourceMapper = resourceMapper;
        this.resourceSelectionMapper = resourceSelectionMapper;
        this.tosStorageService = tosStorageService;
    }

    public List<McpResourceEntity> listAccessibleResources(McpUserContext context) {
        if (context == null || context.tokenId() == null || context.allowedResources().isEmpty()) {
            return List.of();
        }
        Set<String> selectedUris = selectedUris(context.tokenId());
        if (selectedUris.isEmpty()) {
            return List.of();
        }
        return resourceMapper.findPublished().stream()
                .filter(item -> context.allowedResources().contains(item.getResourceUri()))
                .filter(item -> selectedUris.contains(item.getResourceUri()))
                .toList();
    }

    public McpResourceEntity findAccessibleByUri(McpUserContext context, String uri) {
        if (!canAccess(context, uri)) {
            return null;
        }
        return resourceMapper.findEnabledByUri(uri);
    }

    public String readText(McpResourceEntity entity) {
        if (entity == null || entity.getObjectKey() == null || entity.getObjectKey().isBlank()) {
            return "";
        }
        return tosStorageService.downloadString(entity.getObjectKey());
    }

    private boolean canAccess(McpUserContext context, String uri) {
        if (context == null || uri == null || uri.isBlank()) {
            return false;
        }
        return context.allowedResources().contains(uri) && selectedUris(context.tokenId()).contains(uri);
    }

    private Set<String> selectedUris(Long tokenId) {
        if (tokenId == null) {
            return Set.of();
        }
        return resourceSelectionMapper.findEnabledByTokenId(tokenId).stream()
                .map(McpUserResourceSelectionEntity::getResourceUri)
                .collect(Collectors.toSet());
    }
}
