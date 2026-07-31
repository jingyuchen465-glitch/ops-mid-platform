package com.bear.mcp.single.admin.controller;

import com.bear.mcp.single.admin.res.AdminResourceRes;
import com.bear.mcp.single.admin.service.AdminResourceService;
import com.bear.mcp.single.common.api.ApiResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 后台 Resource 选项接口。 */
@RestController
@CrossOrigin
@RequestMapping("/api/admin")
public class AdminResourceController {
    private final AdminResourceService resourceService;

    public AdminResourceController(AdminResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/resources")
    public ApiResponse<List<AdminResourceRes>> list() {
        return ApiResponse.success(resourceService.list());
    }

    @PostMapping("/resources/{id}/enable")
    public ApiResponse<AdminResourceRes> enable(@PathVariable Long id) {
        return ApiResponse.success(resourceService.enable(id));
    }

    @PostMapping("/resources/{id}/disable")
    public ApiResponse<AdminResourceRes> disable(@PathVariable Long id) {
        return ApiResponse.success(resourceService.disable(id));
    }
}
