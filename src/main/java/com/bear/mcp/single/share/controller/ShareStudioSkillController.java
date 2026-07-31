package com.bear.mcp.single.share.controller;

import com.bear.mcp.single.common.api.ApiResponse;
import com.bear.mcp.single.share.req.ShareStudioSkillPresignReq;
import com.bear.mcp.single.share.req.ShareStudioSkillSaveReq;
import com.bear.mcp.single.share.res.ShareStudioSkillPresignDownloadRes;
import com.bear.mcp.single.share.res.ShareStudioSkillPresignRes;
import com.bear.mcp.single.share.res.ShareStudioSkillRes;
import com.bear.mcp.single.share.service.ShareStudioSkillService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 创作空间 Skill 接口。 */
@RestController
@RequestMapping("/api/share/studio/skills")
public class ShareStudioSkillController {
    private final ShareStudioSkillService studioSkillService;

    public ShareStudioSkillController(ShareStudioSkillService studioSkillService) {
        this.studioSkillService = studioSkillService;
    }

    @GetMapping
    public ApiResponse<List<ShareStudioSkillRes>> list(HttpServletRequest request) {
        return ApiResponse.success(studioSkillService.list(currentUserId(request)));
    }

    @PostMapping("/presign-upload")
    public ApiResponse<ShareStudioSkillPresignRes> presignUpload(HttpServletRequest request,
                                                                 @Valid @RequestBody ShareStudioSkillPresignReq req) {
        return ApiResponse.success(studioSkillService.presignUpload(currentUserId(request), req));
    }

    @GetMapping("/{id}/presign-download")
    public ApiResponse<ShareStudioSkillPresignDownloadRes> presignDownload(HttpServletRequest request,
                                                                           @PathVariable Long id) {
        return ApiResponse.success(studioSkillService.presignDownload(currentUserId(request), id));
    }

    @PostMapping
    public ApiResponse<ShareStudioSkillRes> create(HttpServletRequest request,
                                                   @Valid @RequestBody ShareStudioSkillSaveReq req) {
        return ApiResponse.success(studioSkillService.create(currentUserId(request), req));
    }

    @PutMapping("/{id}")
    public ApiResponse<ShareStudioSkillRes> update(HttpServletRequest request,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody ShareStudioSkillSaveReq req) {
        return ApiResponse.success(studioSkillService.update(currentUserId(request), id, req));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<ShareStudioSkillRes> publish(HttpServletRequest request, @PathVariable Long id) {
        return ApiResponse.success(studioSkillService.publish(currentUserId(request), id));
    }

    @PostMapping("/{id}/publish-private")
    public ApiResponse<ShareStudioSkillRes> publishPrivate(HttpServletRequest request, @PathVariable Long id) {
        return ApiResponse.success(studioSkillService.publishPrivate(currentUserId(request), id));
    }

    @PostMapping("/{id}/unpublish")
    public ApiResponse<ShareStudioSkillRes> unpublish(HttpServletRequest request, @PathVariable Long id) {
        return ApiResponse.success(studioSkillService.unpublish(currentUserId(request), id));
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
