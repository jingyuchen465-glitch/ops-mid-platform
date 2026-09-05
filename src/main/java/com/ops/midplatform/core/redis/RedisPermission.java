package com.ops.midplatform.core.redis;

import java.util.List;

/** Redis capabilities granted to one dynamic tool. */
public record RedisPermission(
        String key,
        List<String> commands,
        List<String> fields,
        Integer maxTtlSeconds
) {
}
