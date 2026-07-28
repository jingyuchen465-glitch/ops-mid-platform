package com.bear.mcp.single.admin.controller;

import com.bear.mcp.single.admin.req.AdminCodeListReq;
import com.bear.mcp.single.admin.req.AdminUserSaveReq;
import com.bear.mcp.single.admin.res.AdminUserRes;
import com.bear.mcp.single.admin.service.AdminUserService;
import com.bear.mcp.single.common.api.ApiResponse;
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
