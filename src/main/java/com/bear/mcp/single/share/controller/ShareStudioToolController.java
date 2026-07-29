package com.bear.mcp.single.share.controller;

import com.bear.mcp.single.common.api.ApiResponse;
import com.bear.mcp.single.share.req.ShareStudioToolDebugReq;
import com.bear.mcp.single.share.req.ShareStudioToolSaveReq;
import com.bear.mcp.single.share.res.ShareStudioToolDebugRes;
import com.bear.mcp.single.share.res.ShareStudioToolRes;
import com.bear.mcp.single.share.service.ShareStudioToolService;
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

/**
 * 创作空间动态 Tool 接口。
 *
 * <p>第 8 课在这里完成“把 API 包装成 MCP Tool”的创作入口，
 * 管理后台仍然只负责治理和展示。</p>
 */
@RestController
@RequestMapping("/api/share/studio/tools")
public class ShareStudioToolController {

    /**
     * 创作空间动态 Tool 服务。
     */
    private final ShareStudioToolService studioToolService;

    public ShareStudioToolController(ShareStudioToolService studioToolService) {
        this.studioToolService = studioToolService;
    }

    /**
     * 查询动态 Tool 列表。
     */
    @GetMapping
    public ApiResponse<List<ShareStudioToolRes>> list() {
        return ApiResponse.success(studioToolService.list());
    }

    /**
     * 新建动态 Tool。
     */
    @PostMapping
    public ApiResponse<ShareStudioToolRes> create(@Valid @RequestBody ShareStudioToolSaveReq req) {
        return ApiResponse.success(studioToolService.create(req));
    }

    /**
     * 更新动态 Tool。
     */
    @PutMapping("/{id}")
    public ApiResponse<ShareStudioToolRes> update(@PathVariable Long id,
                                                  @Valid @RequestBody ShareStudioToolSaveReq req) {
        return ApiResponse.success(studioToolService.update(id, req));
    }

    /**
     * 临时调试未保存的动态 Tool。
     */
    @PostMapping("/debug")
    public ApiResponse<ShareStudioToolDebugRes> debugTemporary(HttpServletRequest request,
                                                               @RequestBody ShareStudioToolDebugReq req) {
        return debugResponse(studioToolService.debugTemporary(
                currentUserId(request),
                currentUsername(request),
                req
        ));
    }

    /**
     * 调试已保存的动态 Tool。
     */
    @PostMapping("/{id}/debug")
    public ApiResponse<ShareStudioToolDebugRes> debug(HttpServletRequest request,
                                                      @PathVariable Long id,
                                                      @RequestBody ShareStudioToolDebugReq req) {
        return debugResponse(studioToolService.debug(
                currentUserId(request),
                currentUsername(request),
                id,
                req
        ));
    }

    /**
     * 发布动态 Tool。
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<ShareStudioToolRes> publish(@PathVariable Long id) {
        return ApiResponse.success(studioToolService.publish(id));
    }

    /**
     * 不公开发布动态 Tool。
     */
    @PostMapping("/{id}/publish-private")
    public ApiResponse<ShareStudioToolRes> publishPrivate(@PathVariable Long id) {
        return ApiResponse.success(studioToolService.publishPrivate(id));
    }

    /**
     * 下线动态 Tool。
     */
    @PostMapping("/{id}/unpublish")
    public ApiResponse<ShareStudioToolRes> unpublish(@PathVariable Long id) {
        return ApiResponse.success(studioToolService.unpublish(id));
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

    private String currentUsername(HttpServletRequest request) {
        Object username = request.getAttribute("adminUsername");
        return username != null ? String.valueOf(username) : "";
    }

    private ApiResponse<ShareStudioToolDebugRes> debugResponse(ShareStudioToolDebugRes res) {
        if (Boolean.TRUE.equals(res.getSuccess())) {
            return ApiResponse.success(res);
        }

        return ApiResponse.fail(400, res.getErrorMessage(), res);
    }
}
