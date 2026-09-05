package com.ops.midplatform.publicapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** 公开 ops-skill CLI 安装资源。 */
@RestController
@RequestMapping("/api/public/ops-skill")
public class PublicSkillCliController {
    private static final String PUBLIC_PREFIX = "/api/public/ops-skill";

    @GetMapping(value = "/install.sh", produces = "application/x-sh")
    public ResponseEntity<String> installSh(HttpServletRequest request) throws IOException {
        String content = StreamUtils.copyToString(
                new ClassPathResource("ops-skill/install.sh").getInputStream(),
                StandardCharsets.UTF_8);
        content = content.replace("{{BASE_URL}}", buildBaseUrl(request));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"install.sh\"")
                .body(content);
    }

    @GetMapping(value = "/ops-skill", produces = "application/x-sh")
    public ResponseEntity<byte[]> opsSkillCli() throws IOException {
        byte[] content = StreamUtils.copyToByteArray(
                new ClassPathResource("ops-skill/ops-skill").getInputStream());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/x-sh"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ops-skill\"")
                .body(content);
    }

    @GetMapping(value = "/install.md", produces = "text/markdown; charset=UTF-8")
    public ResponseEntity<String> installMd(HttpServletRequest request) throws IOException {
        String baseUrl = buildBaseUrl(request);
        String content = "# ops-skill 安装\n\n"
                + "```bash\n"
                + "curl -fsSL " + baseUrl + PUBLIC_PREFIX + "/install.sh | bash -s -- --cli-only\n"
                + "ops-skill install SKILL0000000001 --base-url " + baseUrl + "\n"
                + "ops-skill install SKILL0000000001 --base-url " + baseUrl + " --codex\n"
                + "```\n";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ops-skill.md\"")
                .body(content);
    }

    private String buildBaseUrl(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isBlank()) {
            scheme = request.getScheme();
        }
        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            host = request.getServerName();
        }
        int port = request.getServerPort();
        boolean defaultPort = ("https".equals(scheme) && port == 443) || ("http".equals(scheme) && port == 80);
        return scheme + "://" + host + (defaultPort ? "" : ":" + port);
    }
}
