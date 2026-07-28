package com.bear.mcp.single.admin.service;

import com.bear.mcp.single.admin.req.AdminLoginReq;
import com.bear.mcp.single.admin.res.AdminLoginRes;
import com.bear.mcp.single.common.exception.BusinessException;
import com.bear.mcp.single.core.entity.McpUserEntity;
import com.bear.mcp.single.core.mapper.McpUserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {
    private final McpUserMapper userMapper;
    private final AdminJwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminAuthService(McpUserMapper userMapper, AdminJwtService jwtService) {
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    public AdminLoginRes login(AdminLoginReq req) {
        McpUserEntity user = userMapper.findEnabledByUsername(req.getUsername());
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        AdminLoginRes res = new AdminLoginRes();
        res.setToken(jwtService.createToken(user.getId(), user.getUsername()));
        res.setUserId(user.getId());
        res.setUsername(user.getUsername());
        res.setDisplayName(user.getDisplayName());
        return res;
    }
}
