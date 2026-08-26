package com.bear.mcp.single.core.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisScriptExecutorTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisPermissionPolicy policy;
    private RedisScriptExecutor executor;

    @BeforeEach
    void setUp() {
        policy = new RedisPermissionPolicy(new ObjectMapper());
        executor = new RedisScriptExecutor(redisTemplate, policy);
    }

    @Test
    void shouldReadOnlyAuthorizedHashFields() {
        List<RedisPermission> permissions = credentialsPermission();
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.multiGet(
                "bear:feishu:app:user:7", List.of("appId", "enabled")))
                .thenReturn(List.of("cli_123", "1"));

        var result = executor.hmget(
                7L, permissions, "bear:feishu:app:user:7", List.of("appId", "enabled"));

        assertThat(result).containsEntry("appId", "cli_123").containsEntry("enabled", "1");
        assertThatThrownBy(() -> executor.hmget(
                7L, permissions, "bear:feishu:app:user:7", List.of("otherField")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("未授权字段");
    }

    @Test
    void shouldEnforceSetExTtlAndValueSize() {
        List<RedisPermission> permissions = policy.parse("""
                [{
                  "key":"bear:feishu:tenant-token:user:{userId}",
                  "commands":["GET","SETEX"],
                  "maxTtlSeconds":7200
                }]
                """);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        assertThat(executor.setEx(
                7L, permissions, "bear:feishu:tenant-token:user:7", "token-json", 3600)).isTrue();
        verify(valueOperations).set(
                "bear:feishu:tenant-token:user:7", "token-json", Duration.ofSeconds(3600));

        assertThatThrownBy(() -> executor.setEx(
                7L, permissions, "bear:feishu:tenant-token:user:7", "token-json", 7201))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ttlSeconds");
        assertThatThrownBy(() -> executor.setEx(
                7L, permissions, "bear:feishu:tenant-token:user:7", "x".repeat(16 * 1024 + 1), 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("16KB");
    }

    private List<RedisPermission> credentialsPermission() {
        return policy.parse("""
                [{
                  "key":"bear:feishu:app:user:{userId}",
                  "commands":["HMGET"],
                  "fields":["appId","appSecret","enabled"]
                }]
                """);
    }
}
