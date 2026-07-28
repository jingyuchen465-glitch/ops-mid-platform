package com.bear.mcp.single.admin.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class AdminJwtServiceTest {
    @Test
    void shouldCreateAndVerifyJwtWithHutool() {
        AdminJwtService service = new AdminJwtService("bear-mcp-single-test-secret", 7200L);

        String token = service.createToken(10001L, "demo-admin");
        AdminJwtService.AdminJwtUser user = service.verify(token);

        assertThat(user.userId()).isEqualTo(10001L);
        assertThat(user.username()).isEqualTo("demo-admin");
    }

    @Test
    void demoAdminPasswordHashShouldMatchAdmin123() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String passwordHash = "$2a$10$0BkEfhsDbdilg8opNJg.4ehtPFmuAp.0wEk.5qms2hcxFToCblbz2";

        assertThat(passwordEncoder.matches("admin123", passwordHash)).isTrue();
    }
}
