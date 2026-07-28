package com.bear.mcp.single.admin.controller;

import com.bear.mcp.single.admin.res.AdminDashboardRes;
import com.bear.mcp.single.admin.service.AdminDashboardService;
import com.bear.mcp.single.common.api.ApiResponse;
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
