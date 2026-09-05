package com.bear.mcp.single.admin.service;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import com.bear.mcp.single.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/** 管理端 JWT 的签发与验签。 */
@Service
public class AdminJwtService {
    private final byte[] secretKey;
    private final long expireSeconds;

    public AdminJwtService(@Value("${bear.admin.jwt-secret}") String secret,
                           @Value("${bear.admin.jwt-expire-seconds}") long expireSeconds) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "bear.admin.jwt-secret 未配置。生产环境必须通过环境变量 BEAR_ADMIN_JWT_SECRET 注入强随机密钥，禁止使用默认值。");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException(
                    "bear.admin.jwt-secret 长度过短（至少 32 字符）。请配置足够强的随机密钥。");
        }
        this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
        this.expireSeconds = expireSeconds;
    }

    public String createToken(Long userId, String username) {
        Date expireTime = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
        return JWT.create()
                .setKey(secretKey)
                .setSubject(String.valueOf(userId))
                .setPayload("username", username)
                .setExpiresAt(expireTime)
                .sign();
    }

    public AdminJwtUser verify(String token) {
        try {
            JWT jwt = JWT.of(token).setKey(secretKey);
            if (!jwt.verify()) {
                throw unauthorized();
            }
            JWTValidator.of(jwt).validateDate();

            Long userId = Long.parseLong(String.valueOf(jwt.getPayload("sub")));
            String username = String.valueOf(jwt.getPayload("username"));
            return new AdminJwtUser(userId, username);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw unauthorized();
        }
    }

    private BusinessException unauthorized() {
        return new BusinessException(401, "未登录或登录已过期");
    }

    public record AdminJwtUser(Long userId, String username) {
    }
}
