package com.ops.midplatform.admin.controller;

import com.ops.midplatform.admin.res.AdminDashboardRes;
import com.ops.midplatform.admin.service.AdminDashboardService;
import com.ops.midplatform.common.api.ApiResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {
    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ApiResponse<AdminDashboardRes> getDashboard() {
        return ApiResponse.success(dashboardService.getDashboard());
    }
}
