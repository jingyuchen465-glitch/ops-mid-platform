package com.bear.mcp.single.admin.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.bear.mcp.single.admin.req.AdminTokenPromptSelectionItemReq;
import com.bear.mcp.single.admin.req.AdminTokenPromptSelectionSaveReq;
import com.bear.mcp.single.admin.req.AdminTokenResourceSelectionItemReq;
import com.bear.mcp.single.admin.req.AdminTokenResourceSelectionSaveReq;
import com.bear.mcp.single.admin.req.AdminTokenSaveReq;
import com.bear.mcp.single.admin.req.AdminTokenSelectionItemReq;
import com.bear.mcp.single.admin.req.AdminTokenSelectionSaveReq;
import com.bear.mcp.single.admin.res.AdminTokenCreatedRes;
import com.bear.mcp.single.admin.res.AdminTokenPromptSelectionRes;
import com.bear.mcp.single.admin.res.AdminTokenResourceSelectionRes;
import com.bear.mcp.single.admin.res.AdminTokenRes;
import com.bear.mcp.single.admin.res.AdminTokenSelectionRes;
import com.bear.mcp.single.common.exception.BusinessException;
import com.bear.mcp.single.share.res.ShareCursorInstallRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import com.bear.mcp.single.core.entity.McpUserPromptSelectionEntity;
import com.bear.mcp.single.core.entity.McpUserResourceSelectionEntity;
import com.bear.mcp.single.core.entity.McpUserTokenEntity;
import com.bear.mcp.single.core.entity.McpUserToolSelectionEntity;
import com.bear.mcp.single.core.mapper.McpUserPromptSelectionMapper;
import com.bear.mcp.single.core.mapper.McpUserResourceSelectionMapper;
import com.bear.mcp.single.core.mapper.McpUserTokenMapper;
import com.bear.mcp.single.core.mapper.McpUserToolSelectionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminTokenService {
    private final McpUserTokenMapper tokenMapper;
    private final McpUserToolSelectionMapper selectionMapper;
    private final McpUserPromptSelectionMapper promptSelectionMapper;
    private final McpUserResourceSelectionMapper resourceSelectionMapper;
    private final ObjectMapper objectMapper;
    private final AES tokenAes;

    public AdminTokenService(McpUserTokenMapper tokenMapper,
                             McpUserToolSelectionMapper selectionMapper,
                             McpUserPromptSelectionMapper promptSelectionMapper,
                             McpUserResourceSelectionMapper resourceSelectionMapper,
                             ObjectMapper objectMapper,
                             @Value("${bear.admin.jwt-secret}") String secret) {
        this.tokenMapper = tokenMapper;
        this.selectionMapper = selectionMapper;
        this.promptSelectionMapper = promptSelectionMapper;
        this.resourceSelectionMapper = resourceSelectionMapper;
        this.objectMapper = objectMapper;
        this.tokenAes = SecureUtil.aes(aesKey(secret));
    }

    public List<AdminTokenRes> list() {
        return tokenMapper.findAll().stream().map(this::toTokenRes).toList();
    }

    public List<AdminTokenRes> listByUserId(Long userId) {
        return tokenMapper.findByUserId(userId).stream().map(this::toTokenRes).toList();
    }

    public AdminTokenCreatedRes create(AdminTokenSaveReq req) {
        McpUserTokenEntity entity = toTokenEntity(req);
        String rawToken = "mcp_" + randomToken();
        entity.setTokenHash(sha256(rawToken));
        entity.setTokenEncrypted(tokenAes.encryptBase64(rawToken));
        entity.setTokenPrefix(rawToken.substring(0, Math.min(16, rawToken.length())) + "...");
        normalize(entity);
        tokenMapper.insert(entity);

        AdminTokenCreatedRes res = new AdminTokenCreatedRes();
        res.setToken(toTokenRes(entity));
        res.setRawToken(rawToken);
        return res;
    }

    public AdminTokenCreatedRes createForUser(Long userId, AdminTokenSaveReq req) {
        req.setUserId(userId);
        return create(req);
    }

    public AdminTokenRes update(Long id, AdminTokenSaveReq req) {
        McpUserTokenEntity entity = toTokenEntity(req);
        entity.setId(id);
        normalize(entity);
        tokenMapper.update(entity);
        return toTokenRes(entity);
    }

    public AdminTokenRes updateForUser(Long userId, Long id, AdminTokenSaveReq req) {
        McpUserTokenEntity oldEntity = tokenMapper.findById(id);
        if (oldEntity == null || !userId.equals(oldEntity.getUserId())) {
            throw new BusinessException(404, "Token 不存在");
        }
        req.setUserId(userId);
        return update(id, req);
    }

    public List<AdminTokenSelectionRes> listSelectionsByUserId(Long userId) {
        Set<Long> tokenIds = tokenMapper.findByUserId(userId).stream().map(McpUserTokenEntity::getId).collect(java.util.stream.Collectors.toSet());
        return selectionMapper.findAll().stream()
                .filter(item -> tokenIds.contains(item.getTokenId()))
                .map(this::toSelectionRes)
                .toList();
    }

    public List<AdminTokenPromptSelectionRes> listPromptSelectionsByUserId(Long userId) {
        Set<Long> tokenIds = tokenMapper.findByUserId(userId).stream().map(McpUserTokenEntity::getId).collect(java.util.stream.Collectors.toSet());
        return promptSelectionMapper.findAll().stream()
                .filter(item -> tokenIds.contains(item.getTokenId()))
                .map(this::toPromptSelectionRes)
                .toList();
    }

    public List<AdminTokenResourceSelectionRes> listResourceSelectionsByUserId(Long userId) {
        Set<Long> tokenIds = tokenMapper.findByUserId(userId).stream().map(McpUserTokenEntity::getId).collect(java.util.stream.Collectors.toSet());
        return resourceSelectionMapper.findAll().stream()
                .filter(item -> tokenIds.contains(item.getTokenId()))
                .map(this::toResourceSelectionRes)
                .toList();
    }

    @Transactional
    public void replaceSelectionsForUser(Long userId, Long tokenId, AdminTokenSelectionSaveReq req) {
        ensureOwner(userId, tokenId);
        replaceSelections(tokenId, req);
    }

    @Transactional
    public void replacePromptSelectionsForUser(Long userId, Long tokenId, AdminTokenPromptSelectionSaveReq req) {
        ensureOwner(userId, tokenId);
        replacePromptSelections(tokenId, req);
    }

    @Transactional
    public void replaceResourceSelectionsForUser(Long userId, Long tokenId, AdminTokenResourceSelectionSaveReq req) {
        ensureOwner(userId, tokenId);
        replaceResourceSelections(tokenId, req);
    }

    public ShareCursorInstallRes cursorInstallLink(Long userId, Long tokenId, String baseUrl) {
        McpUserTokenEntity entity = ensureOwner(userId, tokenId);
        String rawToken = plainToken(entity);
        String mcpUrl = normalizeBaseUrl(baseUrl) + "/mcp";
        String serverName = "bear-mcp-" + entity.getId();
        try {
            Map<String, Object> config = Map.of(
                    "type", "http",
                    "url", mcpUrl,
                    "headers", Map.of("Authorization", "Bearer " + rawToken)
            );
            String configJson = objectMapper.writeValueAsString(config);
            String encodedConfig = Base64.getEncoder().encodeToString(configJson.getBytes(StandardCharsets.UTF_8));
            String deeplink = "cursor://anysphere.cursor-deeplink/mcp/install?name="
                    + URLEncoder.encode(serverName, StandardCharsets.UTF_8)
                    + "&config="
                    + URLEncoder.encode(encodedConfig, StandardCharsets.UTF_8);
            ShareCursorInstallRes res = new ShareCursorInstallRes();
            res.setServerName(serverName);
            res.setConfigJson(configJson);
            res.setDeeplink(deeplink);
            return res;
        } catch (Exception exception) {
            throw new BusinessException(500, "生成 Cursor 配置失败");
        }
    }

    public List<AdminTokenSelectionRes> listSelections() {
        return selectionMapper.findAll().stream().map(this::toSelectionRes).toList();
    }

    public List<AdminTokenPromptSelectionRes> listPromptSelections() {
        return promptSelectionMapper.findAll().stream().map(this::toPromptSelectionRes).toList();
    }

    public List<AdminTokenResourceSelectionRes> listResourceSelections() {
        return resourceSelectionMapper.findAll().stream().map(this::toResourceSelectionRes).toList();
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

    @Transactional
    public void replacePromptSelections(Long tokenId, AdminTokenPromptSelectionSaveReq req) {
        promptSelectionMapper.deleteByTokenId(tokenId);
        for (AdminTokenPromptSelectionItemReq item : safePrompts(req.getPrompts())) {
            McpUserPromptSelectionEntity entity = new McpUserPromptSelectionEntity();
            entity.setTokenId(tokenId);
            entity.setPromptName(item.getPromptName());
            entity.setEnabled(item.getEnabled() == null ? 1 : item.getEnabled());
            promptSelectionMapper.insert(entity);
        }
    }

    @Transactional
    public void replaceResourceSelections(Long tokenId, AdminTokenResourceSelectionSaveReq req) {
        resourceSelectionMapper.deleteByTokenId(tokenId);
        for (AdminTokenResourceSelectionItemReq item : safeResources(req.getResources())) {
            McpUserResourceSelectionEntity entity = new McpUserResourceSelectionEntity();
            entity.setTokenId(tokenId);
            entity.setResourceUri(item.getResourceUri());
            entity.setEnabled(item.getEnabled() == null ? 1 : item.getEnabled());
            resourceSelectionMapper.insert(entity);
        }
    }

    private List<AdminTokenSelectionItemReq> safe(List<AdminTokenSelectionItemReq> values) {
        return values == null ? List.of() : values;
    }

    private List<AdminTokenPromptSelectionItemReq> safePrompts(List<AdminTokenPromptSelectionItemReq> values) {
        return values == null ? List.of() : values;
    }

    private List<AdminTokenResourceSelectionItemReq> safeResources(List<AdminTokenResourceSelectionItemReq> values) {
        return values == null ? List.of() : values;
    }

    private void normalize(McpUserTokenEntity entity) {
        if (entity.getUserId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        if (entity.getTokenName() == null || entity.getTokenName().trim().isEmpty()) {
            throw new BusinessException(400, "Token名称不能为空");
        }
        entity.setTokenName(entity.getTokenName().trim());
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
        res.setCursorInstallable(entity.getTokenEncrypted() != null && !entity.getTokenEncrypted().isBlank()
                || "mcp_dev_token...".equals(entity.getTokenPrefix()));
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

    private AdminTokenPromptSelectionRes toPromptSelectionRes(McpUserPromptSelectionEntity entity) {
        AdminTokenPromptSelectionRes res = new AdminTokenPromptSelectionRes();
        res.setId(entity.getId());
        res.setTokenId(entity.getTokenId());
        res.setPromptName(entity.getPromptName());
        res.setEnabled(entity.getEnabled());
        return res;
    }

    private AdminTokenResourceSelectionRes toResourceSelectionRes(McpUserResourceSelectionEntity entity) {
        AdminTokenResourceSelectionRes res = new AdminTokenResourceSelectionRes();
        res.setId(entity.getId());
        res.setTokenId(entity.getTokenId());
        res.setResourceUri(entity.getResourceUri());
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

    private McpUserTokenEntity ensureOwner(Long userId, Long tokenId) {
        McpUserTokenEntity entity = tokenMapper.findById(tokenId);
        if (entity == null || !userId.equals(entity.getUserId())) {
            throw new BusinessException(404, "Token 不存在");
        }
        return entity;
    }

    private String plainToken(McpUserTokenEntity entity) {
        if (entity.getTokenEncrypted() != null && !entity.getTokenEncrypted().isBlank()) {
            try {
                return tokenAes.decryptStr(entity.getTokenEncrypted());
            } catch (Exception exception) {
                throw new BusinessException(500, "Token 解密失败");
            }
        }
        if ("mcp_dev_token...".equals(entity.getTokenPrefix())) {
            return "mcp_dev_token";
        }
        throw new BusinessException(400, "历史 Token 没有保存明文，无法一键配置到 Cursor，请新建一把 Token");
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(400, "baseUrl 不能为空");
        }
        String value = baseUrl.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private byte[] aesKey(String secret) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(digest, 16);
        } catch (Exception e) {
            throw new IllegalStateException("初始化 Token 加密器失败", e);
        }
    }
}
