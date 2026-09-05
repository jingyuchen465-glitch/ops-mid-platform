package com.ops.midplatform.admin.controller;

import com.ops.midplatform.admin.req.AdminCodeListReq;
import com.ops.midplatform.admin.req.AdminUserSaveReq;
import com.ops.midplatform.admin.res.AdminUserRoleRes;
import com.ops.midplatform.admin.res.AdminUserRes;
import com.ops.midplatform.admin.service.AdminUserService;
import com.ops.midplatform.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserService userService;

    public AdminUserController(AdminUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<AdminUserRes>> list() {
        return ApiResponse.success(userService.list());
    }

    @GetMapping("/role-relations")
    public ApiResponse<List<AdminUserRoleRes>> listRoles() {
        return ApiResponse.success(userService.listRoles());
    }

    @PostMapping
    public ApiResponse<AdminUserRes> create(@Valid @RequestBody AdminUserSaveReq req) {
        return ApiResponse.success(userService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminUserRes> update(@PathVariable Long id,
                                            @Valid @RequestBody AdminUserSaveReq req) {
        return ApiResponse.success(userService.update(id, req));
    }

    @PutMapping("/{userId}/roles")
    public ApiResponse<Void> replaceRoles(@PathVariable Long userId,
                                          @RequestBody AdminCodeListReq req) {
        userService.replaceRoles(userId, req);
        return ApiResponse.success();
    }
}
