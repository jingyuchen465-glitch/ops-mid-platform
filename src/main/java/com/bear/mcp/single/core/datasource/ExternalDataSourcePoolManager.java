package com.bear.mcp.single.core.datasource;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.bear.mcp.single.core.entity.McpDataSourceEntity;
import com.bear.mcp.single.core.mapper.McpDataSourceMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 按数据源主键缓存外部数据库连接池。 */
@Slf4j
@Component
public class ExternalDataSourcePoolManager {
    private final McpDataSourceMapper dataSourceMapper;
    private final ObjectMapper objectMapper;
    private final AES passwordAes;
    private final ConcurrentHashMap<Long, HikariDataSource> pools = new ConcurrentHashMap<>();

    @Value("${bear.datasource.external.pool-max-size:5}")
    private int poolMaxSize;

    public ExternalDataSourcePoolManager(McpDataSourceMapper dataSourceMapper,
                                         ObjectMapper objectMapper,
                                         @Value("${bear.admin.jwt-secret}") String secret) {
        this.dataSourceMapper = dataSourceMapper;
        this.objectMapper = objectMapper;
        this.passwordAes = SecureUtil.aes(aesKey(secret));
    }

    public HikariDataSource getPool(Long datasourceId) {
        McpDataSourceEntity entity = dataSourceMapper.findById(datasourceId);
        if (entity == null) {
            throw new IllegalArgumentException("数据源不存在: " + datasourceId);
        }
        if (!Integer.valueOf(1).equals(entity.getPublishStatus())) {
            throw new IllegalArgumentException("数据源未发布: " + datasourceId);
        }


        return pools.compute(datasourceId, (id, existing) -> {
            if (existing != null && !existing.isClosed()) {
                return existing;
            }
            return buildPool(entity);
        });
    }

    public void evictPool(Long datasourceId) {
        HikariDataSource dataSource = pools.remove(datasourceId);
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private HikariDataSource buildPool(McpDataSourceEntity entity) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("BearSingleExtDS-" + entity.getId());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(entity.getJdbcUrl());
        config.setUsername(entity.getUsername());
        config.setPassword(passwordAes.decryptStr(entity.getPasswordEncrypted()));
        config.setMaximumPoolSize(Math.max(1, poolMaxSize));
        config.setMinimumIdle(0);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        applyExtraProps(config, entity.getExtraJdbcProps());
        log.info("创建外部数据源连接池 id={} key={}", entity.getId(), entity.getDatasourceKey());
        return new HikariDataSource(config);
    }

    private void applyExtraProps(HikariConfig config, String extraJdbcProps) {
        if (extraJdbcProps == null || extraJdbcProps.isBlank()) {
            return;
        }
        try {
            Map<String, Object> props = objectMapper.readValue(extraJdbcProps, new TypeReference<>() {
            });
            props.forEach((key, value) -> {
                if (key != null && value != null) {
                    config.addDataSourceProperty(key, String.valueOf(value));
                }
            });
        } catch (Exception e) {
            log.warn("解析 extra_jdbc_props 失败: {}", e.getMessage());
        }
    }

    private byte[] aesKey(String secret) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(digest, 16);
        } catch (Exception e) {
            throw new IllegalStateException("初始化数据源密码解密器失败", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        pools.forEach((id, dataSource) -> {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        });
        pools.clear();
    }
}
