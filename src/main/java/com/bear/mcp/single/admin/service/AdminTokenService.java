package com.bear.mcp.single.admin.service;

import com.bear.mcp.single.admin.req.AdminTokenSaveReq;
import com.bear.mcp.single.admin.req.AdminTokenSelectionItemReq;
import com.bear.mcp.single.admin.req.AdminTokenSelectionSaveReq;
import com.bear.mcp.single.admin.res.AdminTokenCreatedRes;
import com.bear.mcp.single.admin.res.AdminTokenRes;
import com.bear.mcp.single.admin.res.AdminTokenSelectionRes;
import com.bear.mcp.single.core.entity.McpUserTokenEntity;
import com.bear.mcp.single.core.entity.McpUserToolSelectionEntity;
import com.bear.mcp.single.core.mapper.McpUserTokenMapper;
import com.bear.mcp.single.core.mapper.McpUserToolSelectionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
public class AdminTokenService {
    private final McpUserTokenMapper tokenMapper;
    private final McpUserToolSelectionMapper selectionMapper;

    public AdminTokenService(McpUserTokenMapper tokenMapper,
                             McpUserToolSelectionMapper selectionMapper) {
        this.tokenMapper = tokenMapper;
        this.selectionMapper = selectionMapper;
    }

    public List<AdminTokenRes> list() {
        return tokenMapper.findAll().stream().map(this::toTokenRes).toList();
    }

    public AdminTokenCreatedRes create(AdminTokenSaveReq req) {
        McpUserTokenEntity entity = toTokenEntity(req);
        String rawToken = "mcp_" + randomToken();
        entity.setTokenHash(sha256(rawToken));
        entity.setTokenPrefix(rawToken.substring(0, Math.min(16, rawToken.length())) + "...");
        normalize(entity);
        tokenMapper.insert(entity);

        AdminTokenCreatedRes res = new AdminTokenCreatedRes();
        res.setToken(toTokenRes(entity));
        res.setRawToken(rawToken);
        return res;
    }

    public AdminTokenRes update(Long id, AdminTokenSaveReq req) {
        McpUserTokenEntity entity = toTokenEntity(req);
        entity.setId(id);
        normalize(entity);
        tokenMapper.update(entity);
        return toTokenRes(entity);
    }

    public List<AdminTokenSelectionRes> listSelections() {
        return selectionMapper.findAll().stream().map(this::toSelectionRes).toList();
    }

    @Transactional
    public void replaceSelections(Long tokenId, AdminTokenSelectionSaveReq req) {
        selectionMapper.deleteByTokenId(tokenId);
        for (AdminTokenSelectionItemReq item : safe(req.getTools())) {
            McpUserToolSelectionEntity entity = new McpUserToolSelectionEntity();
            entity.setTokenId(tokenId);
            entity.setToolName(item.getToolName());
            entity.setToolType(item.getToolType());
            entity.setEnabled(item.getEnabled() == null ? 1 : item.getEnabled());
            selectionMapper.insert(entity);
        }
    }

    private List<AdminTokenSelectionItemReq> safe(List<AdminTokenSelectionItemReq> values) {
        return values == null ? List.of() : values;
    }

    private void normalize(McpUserTokenEntity entity) {
        if (entity.getIsActive() == null) {
            entity.setIsActive(1);
        }
        if (entity.getPermissions() == null) {
            entity.setPermissions("[]");
        }
    }

    private McpUserTokenEntity toTokenEntity(AdminTokenSaveReq req) {
        McpUserTokenEntity entity = new McpUserTokenEntity();
        entity.setUserId(req.getUserId());
        entity.setTokenName(req.getTokenName());
        entity.setPermissions(req.getPermissions());
        entity.setExpireTime(req.getExpireTime());
        entity.setIsActive(req.getIsActive());
        return entity;
    }

    private AdminTokenRes toTokenRes(McpUserTokenEntity entity) {
        AdminTokenRes res = new AdminTokenRes();
        res.setId(entity.getId());
        res.setUserId(entity.getUserId());
        res.setTokenName(entity.getTokenName());
        res.setTokenPrefix(entity.getTokenPrefix());
        res.setPermissions(entity.getPermissions());
        res.setExpireTime(entity.getExpireTime());
        res.setLastUsedTime(entity.getLastUsedTime());
        res.setLastUsedIp(entity.getLastUsedIp());
        res.setIsActive(entity.getIsActive());
        res.setCreateTime(entity.getCreateTime());
        return res;
    }

    private AdminTokenSelectionRes toSelectionRes(McpUserToolSelectionEntity entity) {
        AdminTokenSelectionRes res = new AdminTokenSelectionRes();
        res.setId(entity.getId());
        res.setTokenId(entity.getTokenId());
        res.setToolName(entity.getToolName());
        res.setToolType(entity.getToolType());
        res.setEnabled(entity.getEnabled());
        return res;
    }

    private String randomToken() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
