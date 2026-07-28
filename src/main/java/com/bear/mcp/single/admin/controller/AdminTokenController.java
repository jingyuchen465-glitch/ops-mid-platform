package com.bear.mcp.single.admin.controller;

import com.bear.mcp.single.admin.req.AdminTokenSaveReq;
import com.bear.mcp.single.admin.req.AdminTokenSelectionSaveReq;
import com.bear.mcp.single.admin.res.AdminTokenCreatedRes;
import com.bear.mcp.single.admin.res.AdminTokenRes;
import com.bear.mcp.single.admin.res.AdminTokenSelectionRes;
import com.bear.mcp.single.admin.service.AdminTokenService;
import com.bear.mcp.single.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/admin")
public class AdminTokenController {
    private final AdminTokenService tokenService;

    public AdminTokenController(AdminTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/tokens")
    public ApiResponse<List<AdminTokenRes>> list() {
        return ApiResponse.success(tokenService.list());
    }

    @PostMapping("/tokens")
    public ApiResponse<AdminTokenCreatedRes> create(@Valid @RequestBody AdminTokenSaveReq req) {
        return ApiResponse.success(tokenService.create(req));
    }

    @PutMapping("/tokens/{id}")
    public ApiResponse<AdminTokenRes> update(@PathVariable Long id,
                                             @Valid @RequestBody AdminTokenSaveReq req) {
        return ApiResponse.success(tokenService.update(id, req));
    }

    @GetMapping("/token-selections")
    public ApiResponse<List<AdminTokenSelectionRes>> listSelections() {
        return ApiResponse.success(tokenService.listSelections());
    }

    @PutMapping("/tokens/{tokenId}/selections")
    public ApiResponse<Void> replaceSelections(@PathVariable Long tokenId,
                                               @RequestBody AdminTokenSelectionSaveReq req) {
        tokenService.replaceSelections(tokenId, req);
        return ApiResponse.success();
    }
}
