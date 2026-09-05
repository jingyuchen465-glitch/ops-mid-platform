package com.ops.midplatform.core.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedisPermissionPolicyTest {

    private RedisPermissionPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new RedisPermissionPolicy(new ObjectMapper());
    }

    @Test
    void shouldResolveCurrentUserAndAuthorizeExactCommand() {
        List<RedisPermission> permissions = policy.parse("""
                [{
                  "key": "bear:feishu:app:user:{userId}",
                  "commands": ["hmget"],
                  "fields": ["appId", "appSecret", "enabled"]
                }]
                """);

        RedisPermission permission = policy.require(
                42L, permissions, "bear:feishu:app:user:42", "HMGET");

        assertThat(permission.commands()).containsExactly("HMGET");
        assertThat(permission.fields()).containsExactly("appId", "appSecret", "enabled");
    }

    @Test
    void shouldRejectAnotherUsersKey() {
        List<RedisPermission> permissions = policy.parse("""
                [{"key":"bear:feishu:app:user:{userId}","commands":["HMGET"],"fields":["appId"]}]
                """);

        assertThatThrownBy(() -> policy.require(
                42L, permissions, "bear:feishu:app:user:43", "HMGET"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("未绑定Redis权限");
    }

    @Test
    void shouldRejectUnknownPlaceholderAndUnsupportedCommand() {
        assertThatThrownBy(() -> policy.parse("""
                [{"key":"bear:feishu:app:tenant:{tenantId}","commands":["GET"]}]
                """))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未知占位符");

        assertThatThrownBy(() -> policy.parse("""
                [{"key":"bear:feishu:app:user:{userId}","commands":["DEL"]}]
                """))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持的Redis命令");
    }

    @Test
    void shouldRejectSetExOnCredentialHashAndExcessiveTtl() {
        assertThatThrownBy(() -> policy.parse("""
                [{"key":"bear:feishu:app:user:{userId}","commands":["SETEX"],"maxTtlSeconds":60}]
                """))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("凭据Hash禁止SETEX");

        assertThatThrownBy(() -> policy.parse("""
                [{"key":"bear:feishu:tenant-token:user:{userId}","commands":["SETEX"],"maxTtlSeconds":86401}]
                """))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxTtlSeconds");
    }
}
