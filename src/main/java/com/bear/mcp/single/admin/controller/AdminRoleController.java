package com.bear.mcp.single.admin.controller;

import com.bear.mcp.single.admin.req.AdminCodeListReq;
import com.bear.mcp.single.admin.req.AdminRoleSaveReq;
import com.bear.mcp.single.admin.res.AdminRoleRes;
import com.bear.mcp.single.admin.res.AdminRolePromptRes;
import com.bear.mcp.single.admin.res.AdminRoleToolRes;
import com.bear.mcp.single.admin.service.AdminRoleService;
import com.bear.mcp.single.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/admin")
public class AdminRoleController {
    private final AdminRoleService roleService;

    public AdminRoleController(AdminRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public ApiResponse<List<AdminRoleRes>> list() {
        return ApiResponse.success(roleService.list());
    }

    @PostMapping("/roles")
    public ApiResponse<AdminRoleRes> create(@Valid @RequestBody AdminRoleSaveReq req) {
        return ApiResponse.success(roleService.create(req));
    }

    @PutMapping("/roles/{id}")
    public ApiResponse<AdminRoleRes> update(@PathVariable Long id,
                                            @Valid @RequestBody AdminRoleSaveReq req) {
        return ApiResponse.success(roleService.update(id, req));
    }

    @GetMapping("/role-tools")
    public ApiResponse<List<AdminRoleToolRes>> listTools() {
        return ApiResponse.success(roleService.listTools());
    }

    @GetMapping("/role-prompts")
    public ApiResponse<List<AdminRolePromptRes>> listPrompts() {
        return ApiResponse.success(roleService.listPrompts());
    }

    @PutMapping("/roles/{roleCode}/tools")
    public ApiResponse<Void> replaceTools(@PathVariable String roleCode,
                                          @RequestBody AdminCodeListReq req) {
        roleService.replaceTools(roleCode, req);
        return ApiResponse.success();
    }

    @PutMapping("/roles/{roleCode}/prompts")
    public ApiResponse<Void> replacePrompts(@PathVariable String roleCode,
                                            @RequestBody AdminCodeListReq req) {
        roleService.replacePrompts(roleCode, req);
        return ApiResponse.success();
    }
}
