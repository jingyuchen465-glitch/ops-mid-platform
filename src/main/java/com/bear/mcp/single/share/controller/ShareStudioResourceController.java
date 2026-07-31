package com.bear.mcp.single.share.controller;

import com.bear.mcp.single.common.api.ApiResponse;
import com.bear.mcp.single.share.req.ShareStudioResourcePresignReq;
import com.bear.mcp.single.share.req.ShareStudioResourceSaveReq;
import com.bear.mcp.single.share.res.ShareStudioResourcePresignRes;
import com.bear.mcp.single.share.res.ShareStudioResourcePresignDownloadRes;
import com.bear.mcp.single.share.res.ShareStudioResourceRes;
import com.bear.mcp.single.share.service.ShareStudioResourceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 创作空间 Resource 接口。 */
@Slf4j
@RestController
@RequestMapping("/api/share/studio/resources")
public class ShareStudioResourceController {
    private static final String LOG_PREFIX = "[ShareStudioResource]";

    private final ShareStudioResourceService studioResourceService;

    public ShareStudioResourceController(ShareStudioResourceService studioResourceService) {
        this.studioResourceService = studioResourceService;
    }

    @GetMapping
    public ApiResponse<List<ShareStudioResourceRes>> list(HttpServletRequest request) {
        Long userId = currentUserId(request);
        log.info("{} list start userId={}", LOG_PREFIX, userId);
        List<ShareStudioResourceRes> resources = studioResourceService.list(userId);
        log.info("{} list success userId={}, count={}", LOG_PREFIX, userId, resources.size());
        return ApiResponse.success(resources);
    }

    @PostMapping("/presign-upload")
    public ApiResponse<ShareStudioResourcePresignRes> presignUpload(HttpServletRequest request,
                                                                    @Valid @RequestBody ShareStudioResourcePresignReq req) {
        Long userId = currentUserId(request);
        log.info("{} presignUpload start userId={}, resourceUri={}, fileName={}, fileSize={}",
                LOG_PREFIX, userId, req.getResourceUri(), req.getFileName(), req.getFileSize());
        ShareStudioResourcePresignRes res = studioResourceService.presignUpload(userId, req);
        log.info("{} presignUpload success userId={}, resourceUri={}, objectKey={}, expires={}",
                LOG_PREFIX, userId, req.getResourceUri(), res.getObjectKey(), res.getExpires());
        return ApiResponse.success(res);
    }

    @GetMapping("/{id}/presign-download")
    public ApiResponse<ShareStudioResourcePresignDownloadRes> presignDownload(HttpServletRequest request,
                                                                              @PathVariable Long id) {
        Long userId = currentUserId(request);
        log.info("{} presignDownload start userId={}, resourceId={}", LOG_PREFIX, userId, id);
        ShareStudioResourcePresignDownloadRes res = studioResourceService.presignDownload(userId, id);
        log.info("{} presignDownload success userId={}, resourceId={}, expires={}",
                LOG_PREFIX, userId, id, res.getExpires());
        return ApiResponse.success(res);
    }

    @PostMapping
    public ApiResponse<ShareStudioResourceRes> create(HttpServletRequest request,
                                                      @Valid @RequestBody ShareStudioResourceSaveReq req) {
        Long userId = currentUserId(request);
        log.info("{} create start userId={}, resourceUri={}, name={}, fileName={}, publishStatus={}, enabled={}",
                LOG_PREFIX, userId, req.getResourceUri(), req.getName(), req.getFileName(), req.getPublishStatus(), req.getEnabled());
        ShareStudioResourceRes res = studioResourceService.create(userId, req);
        log.info("{} create success userId={}, resourceId={}, resourceUri={}, publishStatus={}, enabled={}",
                LOG_PREFIX, userId, res.getId(), res.getResourceUri(), res.getPublishStatus(), res.getEnabled());
        return ApiResponse.success(res);
    }

    @PutMapping("/{id}")
    public ApiResponse<ShareStudioResourceRes> update(HttpServletRequest request,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody ShareStudioResourceSaveReq req) {
        Long userId = currentUserId(request);
        log.info("{} update start userId={}, resourceId={}, resourceUri={}, name={}, fileName={}, publishStatus={}, enabled={}",
                LOG_PREFIX, userId, id, req.getResourceUri(), req.getName(), req.getFileName(), req.getPublishStatus(), req.getEnabled());
        ShareStudioResourceRes res = studioResourceService.update(userId, id, req);
        log.info("{} update success userId={}, resourceId={}, resourceUri={}, publishStatus={}, enabled={}",
                LOG_PREFIX, userId, res.getId(), res.getResourceUri(), res.getPublishStatus(), res.getEnabled());
        return ApiResponse.success(res);
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<ShareStudioResourceRes> publish(HttpServletRequest request, @PathVariable Long id) {
        Long userId = currentUserId(request);
        log.info("{} publish start userId={}, resourceId={}", LOG_PREFIX, userId, id);
        ShareStudioResourceRes res = studioResourceService.publish(userId, id);
        log.info("{} publish success userId={}, resourceId={}, publishStatus={}, enabled={}",
                LOG_PREFIX, userId, res.getId(), res.getPublishStatus(), res.getEnabled());
        return ApiResponse.success(res);
    }

    @PostMapping("/{id}/publish-private")
    public ApiResponse<ShareStudioResourceRes> publishPrivate(HttpServletRequest request, @PathVariable Long id) {
        Long userId = currentUserId(request);
        log.info("{} publishPrivate start userId={}, resourceId={}", LOG_PREFIX, userId, id);
        ShareStudioResourceRes res = studioResourceService.publishPrivate(userId, id);
        log.info("{} publishPrivate success userId={}, resourceId={}, publishStatus={}, enabled={}",
                LOG_PREFIX, userId, res.getId(), res.getPublishStatus(), res.getEnabled());
        return ApiResponse.success(res);
    }

    @PostMapping("/{id}/unpublish")
    public ApiResponse<ShareStudioResourceRes> unpublish(HttpServletRequest request, @PathVariable Long id) {
        Long userId = currentUserId(request);
        log.info("{} unpublish start userId={}, resourceId={}", LOG_PREFIX, userId, id);
        ShareStudioResourceRes res = studioResourceService.unpublish(userId, id);
        log.info("{} unpublish success userId={}, resourceId={}, publishStatus={}, enabled={}",
                LOG_PREFIX, userId, res.getId(), res.getPublishStatus(), res.getEnabled());
        return ApiResponse.success(res);
    }

    private Long currentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("adminUserId");
        if (userId instanceof Long value) {
            return value;
        }
        if (userId instanceof Number value) {
            return value.longValue();
        }
        return Long.valueOf(String.valueOf(userId));
    }
}
