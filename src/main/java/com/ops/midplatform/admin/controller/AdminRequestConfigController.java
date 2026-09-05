package com.bear.mcp.single.admin.controller;

import com.bear.mcp.single.admin.req.AdminRequestConfigSaveReq;
import com.bear.mcp.single.admin.res.AdminRequestConfigRes;
import com.bear.mcp.single.admin.service.AdminRequestConfigService;
import com.bear.mcp.single.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/admin/request-configs")
public class AdminRequestConfigController {
    private final AdminRequestConfigService requestConfigService;

    public AdminRequestConfigController(AdminRequestConfigService requestConfigService) {
        this.requestConfigService = requestConfigService;
    }

    @GetMapping
    public ApiResponse<List<AdminRequestConfigRes>> list() {
        return ApiResponse.success(requestConfigService.list());
    }

    @PostMapping
    public ApiResponse<AdminRequestConfigRes> create(@Valid @RequestBody AdminRequestConfigSaveReq req) {
        return ApiResponse.success(requestConfigService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminRequestConfigRes> update(@PathVariable Long id,
                                                     @Valid @RequestBody AdminRequestConfigSaveReq req) {
        return ApiResponse.success(requestConfigService.update(id, req));
    }
}
