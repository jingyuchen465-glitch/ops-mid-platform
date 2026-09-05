package com.ops.midplatform.core.groovy;

import java.util.List;
import java.util.Map;

import com.ops.midplatform.core.redis.RedisPermission;

/**
 * Groovy 脚本执行上下文。
 *
 * <p>DynamicToolService 调用 GroovyScriptEngine 时，会把当前用户、工具名、参数和白名单放进这里。</p>
 */
public record ScriptContext(
        /** MCP tools/call 传入的工具参数。 */
        Map<String, Object> params,
        /** 当前调用用户 ID。 */
        Long userId,
        /** 当前调用用户名。 */
        String userName,
        /** 当前动态工具名。 */
        String toolName,
        /** 当前脚本允许调用的 request config key 白名单。 */
        List<String> linkedRequestKeys,
        /** 当前脚本允许访问的数据源 id 白名单。 */
        List<Long> linkedDataSourceIds,
        /** 当前脚本允许访问的Redis key、命令和字段白名单。 */
        List<RedisPermission> linkedRedisPermissions,
        /** 脚本最大执行时间，单位毫秒。 */
        long timeoutMs
) {
}
