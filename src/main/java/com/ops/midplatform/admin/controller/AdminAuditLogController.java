package com.bear.mcp.single.admin.controller;

import com.bear.mcp.single.admin.res.AdminAuditLogRes;
import com.bear.mcp.single.admin.service.AdminAuditLogService;
import com.bear.mcp.single.common.api.ApiResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditLogController {
    private final AdminAuditLogService auditLogService;

    public AdminAuditLogController(AdminAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AdminAuditLogRes>> list() {
        return ApiResponse.success(auditLogService.listRecent());
    }
}
