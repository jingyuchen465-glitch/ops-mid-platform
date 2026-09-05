package com.ops.midplatform.core.redis;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Executes the small, explicitly allow-listed Redis command set available to Groovy tools. */
@Service
public class RedisScriptExecutor {

    private static final int MAX_VALUE_BYTES = 16 * 1024;

    private final StringRedisTemplate redisTemplate;
    private final RedisPermissionPolicy permissionPolicy;

    public RedisScriptExecutor(StringRedisTemplate redisTemplate,
                               RedisPermissionPolicy permissionPolicy) {
        this.redisTemplate = redisTemplate;
        this.permissionPolicy = permissionPolicy;
    }

    public Map<String, String> hmget(Long userId,
                                     List<RedisPermission> permissions,
                                     String key,
                                     List<String> fields) {
        RedisPermission permission = permissionPolicy.require(
                userId, permissions, key, RedisPermissionPolicy.COMMAND_HMGET);
        List<String> requestedFields = fields != null ? fields.stream().map(String::trim).toList() : List.of();
        if (requestedFields.isEmpty()) {
            throw new IllegalArgumentException("HMGET fields不能为空");
        }
        if (!permission.fields().containsAll(requestedFields)) {
            throw new SecurityException("HMGET包含未授权字段");
        }

        HashOperations<String, Object, Object> operations = redisTemplate.opsForHash();
        List<Object> values = operations.multiGet(key, requestedFields.stream().map(value -> (Object) value).toList());
        if (values == null) {
            values = List.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (int index = 0; index < requestedFields.size(); index++) {
            Object value = index < values.size() ? values.get(index) : null;
            result.put(requestedFields.get(index), value != null ? String.valueOf(value) : null);
        }
        return result;
    }

    public String get(Long userId, List<RedisPermission> permissions, String key) {
        permissionPolicy.require(userId, permissions, key, RedisPermissionPolicy.COMMAND_GET);
        return redisTemplate.opsForValue().get(key);
    }

    public boolean setEx(Long userId,
                         List<RedisPermission> permissions,
                         String key,
                         String value,
                         long ttlSeconds) {
        RedisPermission permission = permissionPolicy.require(
                userId, permissions, key, RedisPermissionPolicy.COMMAND_SETEX);
        if (ttlSeconds <= 0 || ttlSeconds > permission.maxTtlSeconds()) {
            throw new IllegalArgumentException("SETEX ttlSeconds超出授权范围");
        }
        if (value == null) {
            throw new IllegalArgumentException("SETEX value不能为空");
        }
        if (value.getBytes(StandardCharsets.UTF_8).length > MAX_VALUE_BYTES) {
            throw new IllegalArgumentException("SETEX value超过16KB限制");
        }
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
        return true;
    }
}
