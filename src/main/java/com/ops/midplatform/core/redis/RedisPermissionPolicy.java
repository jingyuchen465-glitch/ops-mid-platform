package com.bear.mcp.single.core.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses, normalizes, and enforces the Redis capabilities stored on a dynamic tool. */
@Component
public class RedisPermissionPolicy {

    public static final String COMMAND_HMGET = "HMGET";
    public static final String COMMAND_GET = "GET";
    public static final String COMMAND_SETEX = "SETEX";
    public static final int DEFAULT_MAX_TTL_SECONDS = 7200;
    private static final int MAX_KEY_LENGTH = 512;
    private static final int MAX_TTL_SECONDS = 86400;
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^}]+)}");
    private static final Set<String> SUPPORTED_COMMANDS = Set.of(COMMAND_HMGET, COMMAND_GET, COMMAND_SETEX);
    private static final String FEISHU_CREDENTIAL_KEY_PREFIX = "bear:feishu:app:user:";

    private final ObjectMapper objectMapper;

    public RedisPermissionPolicy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<RedisPermission> parse(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<RedisPermission> permissions = objectMapper.readValue(json, new TypeReference<>() {
            });
            return permissions.stream().map(this::normalize).toList();
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Redis权限JSON格式不正确", exception);
        }
    }

    public String toJson(List<RedisPermission> permissions) {
        try {
            return objectMapper.writeValueAsString(permissions != null ? permissions : List.of());
        } catch (Exception exception) {
            throw new IllegalArgumentException("Redis权限JSON序列化失败", exception);
        }
    }

    public RedisPermission require(Long userId,
                                   List<RedisPermission> permissions,
                                   String key,
                                   String command) {
        if (userId == null) {
            throw new IllegalArgumentException("当前调用缺少用户身份，不能访问Redis");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Redis key不能为空");
        }
        String normalizedCommand = normalizeCommand(command);
        for (RedisPermission permission : permissions != null ? permissions : List.<RedisPermission>of()) {
            String resolvedKey = resolveKey(permission.key(), userId);
            if (resolvedKey.equals(key) && permission.commands().contains(normalizedCommand)) {
                return permission;
            }
        }
        throw new SecurityException("当前动态工具未绑定Redis权限: " + normalizedCommand + " " + key);
    }

    public String resolveKey(String keyTemplate, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        validateKeyTemplate(keyTemplate);
        return keyTemplate.replace("{userId}", String.valueOf(userId));
    }

    private RedisPermission normalize(RedisPermission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Redis权限项不能为空");
        }
        String key = permission.key() != null ? permission.key().trim() : "";
        validateKeyTemplate(key);

        LinkedHashSet<String> commands = new LinkedHashSet<>();
        for (String command : permission.commands() != null ? permission.commands() : List.<String>of()) {
            String normalized = normalizeCommand(command);
            if (!SUPPORTED_COMMANDS.contains(normalized)) {
                throw new IllegalArgumentException("不支持的Redis命令: " + normalized);
            }
            commands.add(normalized);
        }
        if (commands.isEmpty()) {
            throw new IllegalArgumentException("Redis权限必须声明commands");
        }
        if (key.startsWith(FEISHU_CREDENTIAL_KEY_PREFIX) && commands.contains(COMMAND_SETEX)) {
            throw new IllegalArgumentException("飞书凭据Hash禁止SETEX写入");
        }

        LinkedHashSet<String> fields = new LinkedHashSet<>();
        for (String field : permission.fields() != null ? permission.fields() : List.<String>of()) {
            if (field == null || field.isBlank()) {
                throw new IllegalArgumentException("Redis Hash字段不能为空");
            }
            fields.add(field.trim());
        }
        if (commands.contains(COMMAND_HMGET) && fields.isEmpty()) {
            throw new IllegalArgumentException("HMGET权限必须声明fields白名单");
        }

        int maxTtl = permission.maxTtlSeconds() != null
                ? permission.maxTtlSeconds() : DEFAULT_MAX_TTL_SECONDS;
        if (commands.contains(COMMAND_SETEX) && (maxTtl <= 0 || maxTtl > MAX_TTL_SECONDS)) {
            throw new IllegalArgumentException("maxTtlSeconds必须在1到" + MAX_TTL_SECONDS + "之间");
        }
        return new RedisPermission(key, List.copyOf(commands), List.copyOf(fields), maxTtl);
    }

    private void validateKeyTemplate(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Redis权限key不能为空");
        }
        if (key.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException("Redis权限key过长");
        }
        Matcher matcher = PLACEHOLDER.matcher(key);
        while (matcher.find()) {
            if (!"userId".equals(matcher.group(1))) {
                throw new IllegalArgumentException("Redis key包含未知占位符: " + matcher.group());
            }
        }
    }

    private String normalizeCommand(String command) {
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("Redis命令不能为空");
        }
        return command.trim().toUpperCase(Locale.ROOT);
    }
}
