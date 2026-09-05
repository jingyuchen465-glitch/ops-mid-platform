package com.bear.mcp.single.admin.service;

import com.bear.mcp.single.admin.req.AdminLoginReq;
import com.bear.mcp.single.admin.res.AdminLoginRes;
import com.bear.mcp.single.common.exception.BusinessException;
import com.bear.mcp.single.core.entity.McpUserEntity;
import com.bear.mcp.single.core.entity.McpUserRoleEntity;
import com.bear.mcp.single.core.mapper.McpUserMapper;
import com.bear.mcp.single.core.mapper.McpUserRoleMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {
    /**
     * 管理后台角色编码。只有拥有该角色的用户才能登录管理端。
     */
    private static final String ADMIN_ROLE = "ADMIN";

    private final McpUserMapper userMapper;
    private final McpUserRoleMapper userRoleMapper;
    private final AdminJwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminAuthService(McpUserMapper userMapper,
                            McpUserRoleMapper userRoleMapper,
                            AdminJwtService jwtService) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.jwtService = jwtService;
    }

    public AdminLoginRes login(AdminLoginReq req) {
        McpUserEntity user = userMapper.findEnabledByUsername(req.getUsername());
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        boolean isAdmin = userRoleMapper.findByUserId(user.getId()).stream()
                .map(McpUserRoleEntity::getRoleCode)
                .anyMatch(ADMIN_ROLE::equals);
        if (!isAdmin) {
            throw new BusinessException(403, "无管理员权限，禁止登录管理后台");
        }

        AdminLoginRes res = new AdminLoginRes();
        res.setToken(jwtService.createToken(user.getId(), user.getUsername()));
        res.setUserId(user.getId());
        res.setUsername(user.getUsername());
        res.setDisplayName(user.getDisplayName());
        return res;
    }
}
