package com.bear.mcp.single.admin.service;

import com.bear.mcp.single.admin.req.AdminCodeListReq;
import com.bear.mcp.single.admin.req.AdminUserSaveReq;
import com.bear.mcp.single.admin.res.AdminUserRes;
import com.bear.mcp.single.core.entity.McpUserEntity;
import com.bear.mcp.single.core.entity.McpUserRoleEntity;
import com.bear.mcp.single.core.mapper.McpUserMapper;
import com.bear.mcp.single.core.mapper.McpUserRoleMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserService {
    private static final String DEFAULT_PASSWORD = "123456";

    private final McpUserMapper userMapper;
    private final McpUserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminUserService(McpUserMapper userMapper, McpUserRoleMapper userRoleMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
    }

    public List<AdminUserRes> list() {
        return userMapper.findAll().stream().map(this::toRes).toList();
    }

    public AdminUserRes create(AdminUserSaveReq req) {
        McpUserEntity entity = toEntity(req);
        entity.setPasswordHash(passwordEncoder.encode(resolvePassword(req)));
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(1);
        }
        userMapper.insert(entity);
        return toRes(entity);
    }

    public AdminUserRes update(Long id, AdminUserSaveReq req) {
        McpUserEntity entity = toEntity(req);
        entity.setId(id);
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(1);
        }
        userMapper.update(entity);
        return toRes(entity);
    }

    @Transactional
    public void replaceRoles(Long userId, AdminCodeListReq req) {
        userRoleMapper.deleteByUserId(userId);
        for (String roleCode : safe(req.getCodes())) {
            McpUserRoleEntity entity = new McpUserRoleEntity();
            entity.setUserId(userId);
            entity.setRoleCode(roleCode);
            userRoleMapper.insert(entity);
        }
    }

    private List<String> safe(List<String> values) {
        return values == null ? List.of() : values;
    }

    private McpUserEntity toEntity(AdminUserSaveReq req) {
        McpUserEntity entity = new McpUserEntity();
        entity.setUsername(req.getUsername());
        entity.setDisplayName(req.getDisplayName());
        entity.setIsEnabled(req.getIsEnabled());
        return entity;
    }

    private String resolvePassword(AdminUserSaveReq req) {
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            return DEFAULT_PASSWORD;
        }
        return req.getPassword();
    }

    private AdminUserRes toRes(McpUserEntity entity) {
        AdminUserRes res = new AdminUserRes();
        res.setId(entity.getId());
        res.setUsername(entity.getUsername());
        res.setDisplayName(entity.getDisplayName());
        res.setIsEnabled(entity.getIsEnabled());
        res.setCreateTime(entity.getCreateTime());
        res.setUpdateTime(entity.getUpdateTime());
        return res;
    }
}
