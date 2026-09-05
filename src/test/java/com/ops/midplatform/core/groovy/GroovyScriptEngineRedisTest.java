package com.bear.mcp.single.core.groovy;

import com.bear.mcp.single.core.datasource.ExternalDataSourceSqlExecutor;
import com.bear.mcp.single.core.redis.RedisPermission;
import com.bear.mcp.single.core.redis.RedisScriptExecutor;
import com.bear.mcp.single.core.request.RequestConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroovyScriptEngineRedisTest {

    private GroovyScriptEngine engine;

    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
    }

    @Test
    void shouldRedactCredentialsAndTenantTokenFromScriptResult() {
        RedisScriptExecutor redisExecutor = mock(RedisScriptExecutor.class);
        when(redisExecutor.hmget(9L, permissions(), "bear:feishu:app:user:9",
                List.of("appId", "appSecret", "enabled")))
                .thenReturn(Map.of("appId", "cli_test", "appSecret", "secret-value", "enabled", "1"));
        when(redisExecutor.get(9L, permissions(), "bear:feishu:tenant-token:user:9"))
                .thenReturn("{\"appId\":\"cli_test\",\"tenantAccessToken\":\"tenant-token-value\"}");

        engine = new GroovyScriptEngine(
                mock(RequestConfigService.class),
                mock(ExternalDataSourceSqlExecutor.class),
                redisExecutor,
                new ObjectMapper());

        ScriptResult result = engine.execute("""
                def credentials = runRedis.hmget("bear:feishu:app:user:${userId}",
                    ["appId", "appSecret", "enabled"])
                def token = runRedis.get("bear:feishu:tenant-token:user:${userId}")
                return [appId: credentials.appId, appSecret: credentials.appSecret, cache: token]
                """, new ScriptContext(
                Map.of(), 9L, "admin", "feishu_test", List.of(), List.of(), permissions(), 5000));

        assertThat(result.success()).isTrue();
        assertThat(result.result().toString()).doesNotContain("secret-value", "tenant-token-value");
        assertThat(result.result().toString()).contains("***");
    }

    private List<RedisPermission> permissions() {
        return List.of(
                new RedisPermission(
                        "bear:feishu:app:user:{userId}",
                        List.of("HMGET"),
                        List.of("appId", "appSecret", "enabled"),
                        7200),
                new RedisPermission(
                        "bear:feishu:tenant-token:user:{userId}",
                        List.of("GET", "SETEX"),
                        List.of(),
                        7200)
        );
    }
}
