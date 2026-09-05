package com.bear.mcp.single.share.controller;

import com.bear.mcp.single.admin.req.AdminTokenSaveReq;
import com.bear.mcp.single.admin.req.AdminTokenPromptSelectionSaveReq;
import com.bear.mcp.single.admin.req.AdminTokenResourceSelectionSaveReq;
import com.bear.mcp.single.admin.req.AdminTokenSelectionSaveReq;
import com.bear.mcp.single.admin.res.AdminTokenCreatedRes;
import com.bear.mcp.single.admin.res.AdminTokenPromptSelectionRes;
import com.bear.mcp.single.admin.res.AdminTokenResourceSelectionRes;
import com.bear.mcp.single.admin.res.AdminTokenRes;
import com.bear.mcp.single.admin.res.AdminTokenSelectionRes;
import com.bear.mcp.single.admin.service.AdminTokenService;
import com.bear.mcp.single.common.api.ApiResponse;
import com.bear.mcp.single.share.res.ShareCursorInstallRes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 社区侧当前用户 Token 管理接口。 */
@RestController
@RequestMapping("/api/share/tokens")
public class ShareTokenController {
    private final AdminTokenService tokenService;

    public ShareTokenController(AdminTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping
    public ApiResponse<List<AdminTokenRes>> list(HttpServletRequest request) {
        return ApiResponse.success(tokenService.listByUserId(currentUserId(request)));
    }

    @PostMapping
    public ApiResponse<AdminTokenCreatedRes> create(@RequestBody AdminTokenSaveReq req,
                                                    HttpServletRequest request) {
        return ApiResponse.success(tokenService.createForUser(currentUserId(request), req));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminTokenRes> update(@PathVariable Long id,
                                             @RequestBody AdminTokenSaveReq req,
                                             HttpServletRequest request) {
        return ApiResponse.success(tokenService.updateForUser(currentUserId(request), id, req));
    }

    @GetMapping("/selections")
    public ApiResponse<List<AdminTokenSelectionRes>> selections(HttpServletRequest request) {
        return ApiResponse.success(tokenService.listSelectionsByUserId(currentUserId(request)));
    }

    @GetMapping("/prompt-selections")
    public ApiResponse<List<AdminTokenPromptSelectionRes>> promptSelections(HttpServletRequest request) {
        return ApiResponse.success(tokenService.listPromptSelectionsByUserId(currentUserId(request)));
    }

    @GetMapping("/resource-selections")
    public ApiResponse<List<AdminTokenResourceSelectionRes>> resourceSelections(HttpServletRequest request) {
        return ApiResponse.success(tokenService.listResourceSelectionsByUserId(currentUserId(request)));
    }

    @PutMapping("/{tokenId}/selections")
    public ApiResponse<Void> replaceSelections(@PathVariable Long tokenId,
                                               @RequestBody AdminTokenSelectionSaveReq req,
                                               HttpServletRequest request) {
        tokenService.replaceSelectionsForUser(currentUserId(request), tokenId, req);
        return ApiResponse.success();
    }

    @PutMapping("/{tokenId}/prompt-selections")
    public ApiResponse<Void> replacePromptSelections(@PathVariable Long tokenId,
                                                     @RequestBody AdminTokenPromptSelectionSaveReq req,
                                                     HttpServletRequest request) {
        tokenService.replacePromptSelectionsForUser(currentUserId(request), tokenId, req);
        return ApiResponse.success();
    }

    @PutMapping("/{tokenId}/resource-selections")
    public ApiResponse<Void> replaceResourceSelections(@PathVariable Long tokenId,
                                                       @RequestBody AdminTokenResourceSelectionSaveReq req,
                                                       HttpServletRequest request) {
        tokenService.replaceResourceSelectionsForUser(currentUserId(request), tokenId, req);
        return ApiResponse.success();
    }

    @GetMapping("/{tokenId}/cursor-deeplink")
    public ApiResponse<ShareCursorInstallRes> cursorDeeplink(@PathVariable Long tokenId,
                                                             @RequestParam String baseUrl,
                                                             HttpServletRequest request) {
        return ApiResponse.success(tokenService.cursorInstallLink(currentUserId(request), tokenId, baseUrl));
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("adminUserId");
    }
}
