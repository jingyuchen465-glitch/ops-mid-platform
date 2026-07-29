package com.bear.mcp.single.share.controller;

import com.bear.mcp.single.common.api.ApiResponse;
import com.bear.mcp.single.share.req.ShareStudioPromptDebugReq;
import com.bear.mcp.single.share.req.ShareStudioPromptSaveReq;
import com.bear.mcp.single.share.res.ShareStudioPromptDebugRes;
import com.bear.mcp.single.share.res.ShareStudioPromptRes;
import com.bear.mcp.single.share.service.ShareStudioPromptService;
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

/** 创作空间 Prompt 模板接口。 */
@RestController
@RequestMapping("/api/share/studio/prompts")
public class ShareStudioPromptController {
    private final ShareStudioPromptService studioPromptService;

    public ShareStudioPromptController(ShareStudioPromptService studioPromptService) {
        this.studioPromptService = studioPromptService;
    }

    @GetMapping
    public ApiResponse<List<ShareStudioPromptRes>> list(HttpServletRequest request) {
        return ApiResponse.success(studioPromptService.list(currentUserId(request)));
    }

    @PostMapping
    public ApiResponse<ShareStudioPromptRes> create(HttpServletRequest request,
                                                    @Valid @RequestBody ShareStudioPromptSaveReq req) {
        return ApiResponse.success(studioPromptService.create(currentUserId(request), req));
    }

    @PutMapping("/{id}")
    public ApiResponse<ShareStudioPromptRes> update(HttpServletRequest request,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody ShareStudioPromptSaveReq req) {
        return ApiResponse.success(studioPromptService.update(currentUserId(request), id, req));
    }

    @PostMapping("/debug")
    public ApiResponse<ShareStudioPromptDebugRes> debugTemporary(@RequestBody ShareStudioPromptDebugReq req) {
        return ApiResponse.success(studioPromptService.debugTemporary(req));
    }

    @PostMapping("/{id}/debug")
    public ApiResponse<ShareStudioPromptDebugRes> debug(HttpServletRequest request,
                                                        @PathVariable Long id,
                                                        @RequestBody ShareStudioPromptDebugReq req) {
        return ApiResponse.success(studioPromptService.debug(currentUserId(request), id, req));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<ShareStudioPromptRes> publish(HttpServletRequest request, @PathVariable Long id) {
        return ApiResponse.success(studioPromptService.publish(currentUserId(request), id));
    }

    @PostMapping("/{id}/publish-private")
    public ApiResponse<ShareStudioPromptRes> publishPrivate(HttpServletRequest request, @PathVariable Long id) {
        return ApiResponse.success(studioPromptService.publishPrivate(currentUserId(request), id));
    }

    @PostMapping("/{id}/unpublish")
    public ApiResponse<ShareStudioPromptRes> unpublish(HttpServletRequest request, @PathVariable Long id) {
        return ApiResponse.success(studioPromptService.unpublish(currentUserId(request), id));
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
