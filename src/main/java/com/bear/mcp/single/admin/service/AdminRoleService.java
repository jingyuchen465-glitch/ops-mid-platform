package com.bear.mcp.single.admin.service;

import com.bear.mcp.single.admin.req.AdminCodeListReq;
import com.bear.mcp.single.admin.req.AdminRoleSaveReq;
import com.bear.mcp.single.admin.res.AdminRoleRes;
import com.bear.mcp.single.admin.res.AdminRoleToolRes;
import com.bear.mcp.single.core.entity.McpRoleEntity;
import com.bear.mcp.single.core.entity.McpRoleToolEntity;
import com.bear.mcp.single.core.mapper.McpRoleMapper;
import com.bear.mcp.single.core.mapper.McpRoleToolMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminRoleService {
    private final McpRoleMapper roleMapper;
    private final McpRoleToolMapper roleToolMapper;

    public AdminRoleService(McpRoleMapper roleMapper, McpRoleToolMapper roleToolMapper) {
        this.roleMapper = roleMapper;
        this.roleToolMapper = roleToolMapper;
    }

    public List<AdminRoleRes> list() {
        return roleMapper.findAll().stream().map(this::toRoleRes).toList();
    }

    public AdminRoleRes create(AdminRoleSaveReq req) {
        McpRoleEntity entity = toEntity(req);
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(1);
        }
        roleMapper.insert(entity);
        return toRoleRes(entity);
    }

    public AdminRoleRes update(Long id, AdminRoleSaveReq req) {
        McpRoleEntity entity = toEntity(req);
        entity.setId(id);
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(1);
        }
        roleMapper.update(entity);
        return toRoleRes(entity);
    }

    public List<AdminRoleToolRes> listTools() {
        return roleToolMapper.findAll().stream().map(this::toRoleToolRes).toList();
    }

    @Transactional
    public void replaceTools(String roleCode, AdminCodeListReq req) {
        roleToolMapper.deleteByRoleCode(roleCode);
        for (String toolName : safe(req.getCodes())) {
            McpRoleToolEntity entity = new McpRoleToolEntity();
            entity.setRoleCode(roleCode);
            entity.setToolName(toolName);
            roleToolMapper.insert(entity);
        }
    }

    private List<String> safe(List<String> values) {
        return values == null ? List.of() : values;
    }

    private McpRoleEntity toEntity(AdminRoleSaveReq req) {
        McpRoleEntity entity = new McpRoleEntity();
        entity.setRoleCode(req.getRoleCode());
        entity.setRoleName(req.getRoleName());
        entity.setDescription(req.getDescription());
        entity.setIsEnabled(req.getIsEnabled());
        return entity;
    }

    private AdminRoleRes toRoleRes(McpRoleEntity entity) {
        AdminRoleRes res = new AdminRoleRes();
        res.setId(entity.getId());
        res.setRoleCode(entity.getRoleCode());
        res.setRoleName(entity.getRoleName());
        res.setDescription(entity.getDescription());
        res.setIsEnabled(entity.getIsEnabled());
        res.setCreateTime(entity.getCreateTime());
        res.setUpdateTime(entity.getUpdateTime());
        return res;
    }

    private AdminRoleToolRes toRoleToolRes(McpRoleToolEntity entity) {
        AdminRoleToolRes res = new AdminRoleToolRes();
        res.setId(entity.getId());
        res.setRoleCode(entity.getRoleCode());
        res.setToolName(entity.getToolName());
        res.setCreateTime(entity.getCreateTime());
        return res;
    }
}
