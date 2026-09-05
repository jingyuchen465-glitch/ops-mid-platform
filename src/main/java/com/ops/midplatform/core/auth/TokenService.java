package com.bear.mcp.single.core.auth;

import com.bear.mcp.single.core.entity.McpRoleEntity;
import com.bear.mcp.single.core.entity.McpRolePromptEntity;
import com.bear.mcp.single.core.entity.McpRoleResourceEntity;
import com.bear.mcp.single.core.entity.McpRoleToolEntity;
import com.bear.mcp.single.core.entity.McpUserEntity;
import com.bear.mcp.single.core.entity.McpUserRoleEntity;
import com.bear.mcp.single.core.entity.McpUserTokenEntity;
import com.bear.mcp.single.core.mapper.McpRoleMapper;
import com.bear.mcp.single.core.mapper.McpRolePromptMapper;
import com.bear.mcp.single.core.mapper.McpRoleResourceMapper;
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

    /**
     * mcp_user_token：根据 Token 摘要查询当前请求使用的是哪一把 Token。
     */
    private final McpUserTokenMapper tokenMapper;

    /**
     * mcp_user：Token 归属用户，必须是启用状态。
     */
    private final McpUserMapper userMapper;

    /**
     * mcp_user_role：用户和角色的关系表。
     */
    private final McpUserRoleMapper userRoleMapper;

    /**
     * mcp_role：角色主表，用来过滤被禁用的角色。
     */
    private final McpRoleMapper roleMapper;

    /**
     * mcp_role_tool：角色能使用哪些工具，是 RBAC 权限上限。
     */
    private final McpRoleToolMapper roleToolMapper;

    /**
     * mcp_role_prompt：角色能使用哪些 Prompt，是 Prompt 权限上限。
     */
    private final McpRolePromptMapper rolePromptMapper;

    /**
     * mcp_role_resource：角色能读取哪些 Resource，是 Resource 权限上限。
     */
    private final McpRoleResourceMapper roleResourceMapper;

    public TokenService(McpUserTokenMapper tokenMapper,
                        McpUserMapper userMapper,
                        McpUserRoleMapper userRoleMapper,
                        McpRoleMapper roleMapper,
                        McpRoleToolMapper roleToolMapper,
                        McpRolePromptMapper rolePromptMapper,
                        McpRoleResourceMapper roleResourceMapper) {
        this.tokenMapper = tokenMapper;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.roleToolMapper = roleToolMapper;
        this.rolePromptMapper = rolePromptMapper;
        this.roleResourceMapper = roleResourceMapper;
    }

    public Optional<TokenAuthInfo> validate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        /*
         * 数据库不保存明文 Token，只保存 SHA-256 摘要。
         * 请求进来时重新计算摘要，再和 mcp_user_token.token_hash 比对。
         */
        McpUserTokenEntity tokenEntity = tokenMapper.findActiveByTokenHash(sha256(token.trim()));
        if (tokenEntity == null || expired(tokenEntity)) {
            return Optional.empty();
        }

        /*
         * Token 只是钥匙，真正的调用主体还是用户。
         * 如果用户被禁用，即使 Token 本身有效，也不能继续访问 MCP。
         */
        McpUserEntity userEntity = userMapper.findEnabledById(tokenEntity.getUserId());
        if (userEntity == null) {
            return Optional.empty();
        }

        /*
         * 先查用户绑定了哪些角色编码，再从 mcp_role 里过滤出启用角色。
         * 这样用户角色关系里即使还残留了旧角色，也不会进入最终权限。
         */
        Set<String> requestedRoleCodes = userRoleMapper.findByUserId(userEntity.getId()).stream()
                .map(McpUserRoleEntity::getRoleCode)
                .collect(Collectors.toSet());
        Set<String> activeRoleCodes = requestedRoleCodes.isEmpty() ? Set.of()
                : roleMapper.findEnabledByRoleCodes(requestedRoleCodes).stream()
                .map(McpRoleEntity::getRoleCode)
                .collect(Collectors.toSet());

        /*
         * allowedTools 表示“这个用户通过角色具备的工具资格”。
         * 注意它还不是最终 tools/list 结果，后面还要和 Token 工具选择取交集。
         */
        Set<String> allowedTools = activeRoleCodes.isEmpty() ? Set.of()
                : roleToolMapper.findByRoleCodes(activeRoleCodes).stream()
                .map(McpRoleToolEntity::getToolName)
                .collect(Collectors.toSet());

        /*
         * Prompt 和 Tool 一样走角色资格上限。
         * 最终 prompts/list 还会再和 Token Prompt 选择取交集。
         */
        Set<String> allowedPrompts = activeRoleCodes.isEmpty() ? Set.of()
                : rolePromptMapper.findByRoleCodes(activeRoleCodes).stream()
                .map(McpRolePromptEntity::getPromptName)
                .collect(Collectors.toSet());

        Set<String> allowedResources = activeRoleCodes.isEmpty() ? Set.of()
                : roleResourceMapper.findByRoleCodes(activeRoleCodes).stream()
                .map(McpRoleResourceEntity::getResourceUri)
                .collect(Collectors.toSet());

        /*
         * 只要鉴权通过，就刷新最后使用时间。
         * 它方便管理端观察 Token 是否还在被真实调用。
         */
        tokenMapper.updateLastUsedTime(tokenEntity.getId(), new Date());
        return Optional.of(new TokenAuthInfo(
                tokenEntity.getId(),
                userEntity.getId(),
                userEntity.getUsername(),
                activeRoleCodes,
                allowedTools,
                allowedPrompts,
                allowedResources
        ));
    }

    /**
     * Token 有过期时间时，当前时间晚于过期时间就不能再使用。
     */
    private boolean expired(McpUserTokenEntity entity) {
        return entity.getExpireTime() != null && entity.getExpireTime().before(new Date());
    }

    /**
     * 计算 Token 明文的 SHA-256 摘要。
     *
     * 摘要只用于比对，不能反推出原始 Token。
     */
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
