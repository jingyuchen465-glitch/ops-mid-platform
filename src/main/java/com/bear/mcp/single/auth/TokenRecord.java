package com.bear.mcp.single.auth;

import java.util.Set;

public record TokenRecord(
        Long id,
        Long userId,
        String token,
        String userName,
        Set<String> roles,
        Set<String> allowedTools
) {
}
