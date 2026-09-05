package com.ops.midplatform.admin.controller;

import com.ops.midplatform.admin.req.AdminRequestConfigSaveReq;
import com.ops.midplatform.admin.res.AdminRequestConfigRes;
import com.ops.midplatform.admin.service.AdminRequestConfigService;
import com.ops.midplatform.common.api.ApiResponse;
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
