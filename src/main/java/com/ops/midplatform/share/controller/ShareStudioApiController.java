package com.ops.midplatform.share.controller;

import com.ops.midplatform.common.api.ApiResponse;
import com.ops.midplatform.share.req.ShareStudioApiDebugReq;
import com.ops.midplatform.share.req.ShareStudioApiSaveReq;
import com.ops.midplatform.share.req.ShareStudioApiTemporaryDebugReq;
import com.ops.midplatform.share.res.ShareStudioApiDebugRes;
import com.ops.midplatform.share.res.ShareStudioApiRes;
import com.ops.midplatform.share.service.ShareStudioApiService;
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
 * 创作空间 HTTP API 接口。
 *
 * <p>这些接口给用户侧页面使用，不承接治理后台职责。
 * 课程第 7 课先把“外部 HTTP API 接入、调试、发布”讲清楚，
 * 后续课程再把这些 API 包装成 MCP 动态工具。</p>
 *
 * <p>这里虽然也使用管理员登录后的 JWT 身份，但页面定位不是“管理后台”，
 * 而是普通能力作者的创作空间，所以单独放在 share 包下。</p>
 */
@RestController
@RequestMapping("/api/share/studio/apis")
public class ShareStudioApiController {

    /**
     * 创作空间 API 服务。
     */
    private final ShareStudioApiService studioApiService;

    public ShareStudioApiController(ShareStudioApiService studioApiService) {
        this.studioApiService = studioApiService;
    }

    /**
     * 查询当前登录用户自己创建的 HTTP API。
     *
     * <p>创作空间不是全局治理后台，列表只展示当前用户自己的作品。</p>
     */
    @GetMapping
    public ApiResponse<List<ShareStudioApiRes>> list(HttpServletRequest request) {
        return ApiResponse.success(studioApiService.list(currentUserId(request)));
    }

    /**
     * 保存一条新的 HTTP API 配置。
     *
     * <p>新建时默认还是草稿，不会因为保存就进入社区或变成 MCP Tool。</p>
     */
    @PostMapping
    public ApiResponse<ShareStudioApiRes> create(HttpServletRequest request,
                                                 @Valid @RequestBody ShareStudioApiSaveReq req) {
        return ApiResponse.success(studioApiService.create(currentUserId(request), req));
    }

    /**
     * 修改已经保存的 HTTP API 配置。
     *
     * <p>Controller 只接收 Req、返回 Res，不直接暴露 Entity。</p>
     */
    @PutMapping("/{id}")
    public ApiResponse<ShareStudioApiRes> update(HttpServletRequest request,
                                                 @PathVariable Long id,
                                                 @Valid @RequestBody ShareStudioApiSaveReq req) {
        return ApiResponse.success(studioApiService.update(currentUserId(request), id, req));
    }

    /**
     * 未保存前的临时调试。
     *
     * <p>这是创作空间里“先发送调试，再保存配置”的入口，不会落库。</p>
     */
    @PostMapping("/debug")
    public ApiResponse<ShareStudioApiDebugRes> debugTemporary(HttpServletRequest request,
                                                              @Valid @RequestBody ShareStudioApiTemporaryDebugReq req) {
        return debugResponse(studioApiService.debugTemporary(currentUserId(request), req));
    }

    /**
     * 对已经保存的 API 做调试。
     *
     * <p>用户从列表里点“调试”时走这个入口，配置从数据库读取。</p>
     */
    @PostMapping("/{id}/debug")
    public ApiResponse<ShareStudioApiDebugRes> debug(HttpServletRequest request,
                                                     @PathVariable Long id,
                                                     @RequestBody ShareStudioApiDebugReq req) {
        return debugResponse(studioApiService.debug(currentUserId(request), id, req));
    }

    /**
     * 公开发布。
     *
     * <p>发布状态改为 2，表示已上线且进入社区公开展示。</p>
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<ShareStudioApiRes> publish(HttpServletRequest request,
                                                  @PathVariable Long id) {
        return ApiResponse.success(studioApiService.publish(currentUserId(request), id));
    }

    /**
     * 不公开发布。
     *
     * <p>发布状态改为 1，表示 API 已上线，可供自己后续编排，但不进入社区公开列表。</p>
     */
    @PostMapping("/{id}/publish-private")
    public ApiResponse<ShareStudioApiRes> publishPrivate(HttpServletRequest request,
                                                         @PathVariable Long id) {
        return ApiResponse.success(studioApiService.publishPrivate(currentUserId(request), id));
    }

    /**
     * 下架为草稿。
     *
     * <p>发布状态改为 0，表示不再作为已发布能力使用。</p>
     */
    @PostMapping("/{id}/unpublish")
    public ApiResponse<ShareStudioApiRes> unpublish(HttpServletRequest request,
                                                    @PathVariable Long id) {
        return ApiResponse.success(studioApiService.unpublish(currentUserId(request), id));
    }

    /**
     * 从登录过滤器写入的请求属性中取出当前用户 ID。
     *
     * <p>这里不让前端传 userId，避免用户伪造“替别人创建 API”。</p>
     */
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

    /**
     * 调试失败时返回业务错误码，但保留调试结果数据。
     *
     * <p>这样前端既能弹出失败提示，也能在响应面板里展示错误原因和耗时。</p>
     */
    private ApiResponse<ShareStudioApiDebugRes> debugResponse(ShareStudioApiDebugRes res) {
        if (Boolean.TRUE.equals(res.getSuccess())) {
            return ApiResponse.success(res);
        }

        return ApiResponse.fail(400, res.getErrorMessage(), res);
    }
}
