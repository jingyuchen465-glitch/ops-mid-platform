package com.ops.midplatform.admin.controller;

import com.ops.midplatform.admin.req.AdminLoginReq;
import com.ops.midplatform.admin.res.AdminLoginRes;
import com.ops.midplatform.admin.service.AdminAuthService;
import com.ops.midplatform.common.api.ApiResponse;
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
