package com.bear.mcp.single.admin.service;

import com.bear.mcp.single.admin.req.AdminCodeListReq;
import com.bear.mcp.single.admin.req.AdminRoleSaveReq;
import com.bear.mcp.single.admin.res.AdminRoleRes;
import com.bear.mcp.single.admin.res.AdminRolePromptRes;
import com.bear.mcp.single.admin.res.AdminRoleResourceRes;
import com.bear.mcp.single.admin.res.AdminRoleToolRes;
import com.bear.mcp.single.core.entity.McpRoleEntity;
import com.bear.mcp.single.core.entity.McpRolePromptEntity;
import com.bear.mcp.single.core.entity.McpRoleResourceEntity;
import com.bear.mcp.single.core.entity.McpRoleToolEntity;
import com.bear.mcp.single.core.mapper.McpRoleMapper;
import com.bear.mcp.single.core.mapper.McpRolePromptMapper;
import com.bear.mcp.single.core.mapper.McpRoleResourceMapper;
import com.bear.mcp.single.core.mapper.McpRoleToolMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminRoleService {
    private final McpRoleMapper roleMapper;
    private final McpRoleToolMapper roleToolMapper;
    private final McpRolePromptMapper rolePromptMapper;
    private final McpRoleResourceMapper roleResourceMapper;

    public AdminRoleService(McpRoleMapper roleMapper,
                            McpRoleToolMapper roleToolMapper,
                            McpRolePromptMapper rolePromptMapper,
                            McpRoleResourceMapper roleResourceMapper) {
        this.roleMapper = roleMapper;
        this.roleToolMapper = roleToolMapper;
        this.rolePromptMapper = rolePromptMapper;
        this.roleResourceMapper = roleResourceMapper;
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

    public List<AdminRolePromptRes> listPrompts() {
        return rolePromptMapper.findAll().stream().map(this::toRolePromptRes).toList();
    }

    public List<AdminRoleResourceRes> listResources() {
        return roleResourceMapper.findAll().stream().map(this::toRoleResourceRes).toList();
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

    @Transactional
    public void replacePrompts(String roleCode, AdminCodeListReq req) {
        rolePromptMapper.deleteByRoleCode(roleCode);
        for (String promptName : safe(req.getCodes())) {
            McpRolePromptEntity entity = new McpRolePromptEntity();
            entity.setRoleCode(roleCode);
            entity.setPromptName(promptName);
            rolePromptMapper.insert(entity);
        }
    }

    @Transactional
    public void replaceResources(String roleCode, AdminCodeListReq req) {
        roleResourceMapper.deleteByRoleCode(roleCode);
        for (String resourceUri : safe(req.getCodes())) {
            McpRoleResourceEntity entity = new McpRoleResourceEntity();
            entity.setRoleCode(roleCode);
            entity.setResourceUri(resourceUri);
            roleResourceMapper.insert(entity);
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

    private AdminRolePromptRes toRolePromptRes(McpRolePromptEntity entity) {
        AdminRolePromptRes res = new AdminRolePromptRes();
        res.setId(entity.getId());
        res.setRoleCode(entity.getRoleCode());
        res.setPromptName(entity.getPromptName());
        res.setCreateTime(entity.getCreateTime());
        return res;
    }

    private AdminRoleResourceRes toRoleResourceRes(McpRoleResourceEntity entity) {
        AdminRoleResourceRes res = new AdminRoleResourceRes();
        res.setId(entity.getId());
        res.setRoleCode(entity.getRoleCode());
        res.setResourceUri(entity.getResourceUri());
        res.setCreateTime(entity.getCreateTime());
        return res;
    }
}
