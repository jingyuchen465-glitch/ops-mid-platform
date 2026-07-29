package com.bear.mcp.single.admin.controller;

import com.bear.mcp.single.admin.req.AdminDataSourceSaveReq;
import com.bear.mcp.single.admin.res.AdminDataSourceRes;
import com.bear.mcp.single.admin.service.AdminDataSourceService;
import com.bear.mcp.single.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/admin/data-sources")
public class AdminDataSourceController {
    private final AdminDataSourceService dataSourceService;

    public AdminDataSourceController(AdminDataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    @GetMapping
    public ApiResponse<List<AdminDataSourceRes>> list() {
        return ApiResponse.success(dataSourceService.list());
    }

    @PostMapping
    public ApiResponse<AdminDataSourceRes> create(@Valid @RequestBody AdminDataSourceSaveReq req) {
        return ApiResponse.success(dataSourceService.create(req));
    }

    @PostMapping("/test-connection")
    public ApiResponse<Void> testConnection(@Valid @RequestBody AdminDataSourceSaveReq req) {
        dataSourceService.testConnection(req);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminDataSourceRes> update(@PathVariable Long id,
                                                  @Valid @RequestBody AdminDataSourceSaveReq req) {
        return ApiResponse.success(dataSourceService.update(id, req));
    }

    @PostMapping("/{id}/test-connection")
    public ApiResponse<Void> testConnection(@PathVariable Long id,
                                            @Valid @RequestBody AdminDataSourceSaveReq req) {
        dataSourceService.testConnection(id, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dataSourceService.delete(id);
        return ApiResponse.success();
    }
}
