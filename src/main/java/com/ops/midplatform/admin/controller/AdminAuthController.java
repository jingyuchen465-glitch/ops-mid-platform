package com.bear.mcp.single.admin.controller;

import com.bear.mcp.single.admin.req.AdminLoginReq;
import com.bear.mcp.single.admin.res.AdminLoginRes;
import com.bear.mcp.single.admin.service.AdminAuthService;
import com.bear.mcp.single.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    private final AdminAuthService authService;
    public AdminAuthController(AdminAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginRes> login(@Valid @RequestBody AdminLoginReq req) {
        return ApiResponse.success(authService.login(req));
    }
}
