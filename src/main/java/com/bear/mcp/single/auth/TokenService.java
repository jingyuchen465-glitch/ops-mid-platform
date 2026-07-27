package com.bear.mcp.single.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private final Map<String, TokenRecord> tokens = new ConcurrentHashMap<>();

    public TokenService(@Value("${bear.mcp.demo-token:mcp_dev_token}") String demoToken) {
        tokens.put(demoToken, new TokenRecord(
                1L,
                10001L,
                demoToken,
                "demo-admin",
                Set.of("ADMIN", "DEVELOPER"),
                Set.of("hello", "current_time", "system_info", "calculate", "echo_dynamic")
        ));
    }

    public Optional<TokenRecord> validate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokens.get(token.trim()));
    }
}
