package com.bear.mcp.single.core.auth;

import com.bear.mcp.single.core.entity.McpRoleEntity;
import com.bear.mcp.single.core.entity.McpRoleToolEntity;
import com.bear.mcp.single.core.entity.McpUserEntity;
import com.bear.mcp.single.core.entity.McpUserRoleEntity;
import com.bear.mcp.single.core.entity.McpUserTokenEntity;
import com.bear.mcp.single.core.mapper.McpRoleMapper;
import com.bear.mcp.single.core.mapper.McpRoleToolMapper;
import com.bear.mcp.single.core.mapper.McpUserMapper;
import com.bear.mcp.single.core.mapper.McpUserRoleMapper;
import com.bear.mcp.single.core.mapper.McpUserTokenMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TokenService {

    private final McpUserTokenMapper tokenMapper;
    private final McpUserMapper userMapper;
    private final McpUserRoleMapper userRoleMapper;
    private final McpRoleMapper roleMapper;
    private final McpRoleToolMapper roleToolMapper;

    public TokenService(McpUserTokenMapper tokenMapper,
                        McpUserMapper userMapper,
                        McpUserRoleMapper userRoleMapper,
                        McpRoleMapper roleMapper,
                        McpRoleToolMapper roleToolMapper) {
        this.tokenMapper = tokenMapper;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.roleToolMapper = roleToolMapper;
    }

    public Optional<TokenAuthInfo> validate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        McpUserTokenEntity tokenEntity = tokenMapper.findActiveByTokenHash(sha256(token.trim()));
        if (tokenEntity == null || expired(tokenEntity)) {
            return Optional.empty();
        }
        McpUserEntity userEntity = userMapper.findEnabledById(tokenEntity.getUserId());
        if (userEntity == null) {
            return Optional.empty();
        }
        Set<String> requestedRoleCodes = userRoleMapper.findByUserId(userEntity.getId()).stream()
                .map(McpUserRoleEntity::getRoleCode)
                .collect(Collectors.toSet());
        Set<String> activeRoleCodes = requestedRoleCodes.isEmpty() ? Set.of()
                : roleMapper.findEnabledByRoleCodes(requestedRoleCodes).stream()
                .map(McpRoleEntity::getRoleCode)
                .collect(Collectors.toSet());
        Set<String> allowedTools = activeRoleCodes.isEmpty() ? Set.of()
                : roleToolMapper.findByRoleCodes(activeRoleCodes).stream()
                .map(McpRoleToolEntity::getToolName)
                .collect(Collectors.toSet());

        tokenMapper.updateLastUsedTime(tokenEntity.getId(), new Date());
        return Optional.of(new TokenAuthInfo(
                tokenEntity.getId(),
                userEntity.getId(),
                userEntity.getUsername(),
                activeRoleCodes,
                allowedTools
        ));
    }

    private boolean expired(McpUserTokenEntity entity) {
        return entity.getExpireTime() != null && entity.getExpireTime().before(new Date());
    }

    private String sha256(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
