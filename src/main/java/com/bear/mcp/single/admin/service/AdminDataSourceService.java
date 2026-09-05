package com.bear.mcp.single.admin.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.bear.mcp.single.admin.req.AdminDataSourceSaveReq;
import com.bear.mcp.single.admin.res.AdminDataSourceRes;
import com.bear.mcp.single.common.exception.BusinessException;
import com.bear.mcp.single.core.context.McpUserContextHolder;
import com.bear.mcp.single.core.entity.McpDataSourceEntity;
import com.bear.mcp.single.core.mapper.McpDataSourceMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** 管理端数据源 CRUD。 */
@Service
public class AdminDataSourceService {
    private static final int PUBLISH_DRAFT = 0;
    private static final int PUBLISH_LIVE = 1;

    private final McpDataSourceMapper mapper;
    private final ObjectMapper objectMapper;
    private final AES passwordAes;

    public AdminDataSourceService(McpDataSourceMapper mapper,
                                  ObjectMapper objectMapper,
                                  @Value("${bear.admin.jwt-secret}") String secret,
                                  @Value("${bear.admin.data-secret:}") String dataSecret) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        /*
         * 数据源密码加密使用独立的数据加密密钥，避免与 JWT 签名密钥混用。
         * 未单独配置时回落到 jwt-secret，以保持对旧数据的兼容。
         */
        this.passwordAes = SecureUtil.aes(aesKey(dataSecret != null && !dataSecret.isBlank() ? dataSecret : secret));
    }

    public List<AdminDataSourceRes> list() {
        return mapper.findAll().stream().map(this::toRes).toList();
    }

    public AdminDataSourceRes create(AdminDataSourceSaveReq req) {
        normalize(req);
        if (mapper.findByDatasourceKey(req.getDatasourceKey()) != null) {
            throw new BusinessException(400, "数据源Key已存在: " + req.getDatasourceKey());
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new BusinessException(400, "新建数据源时密码不能为空");
        }
        testConnection(req, req.getPassword());

        McpDataSourceEntity entity = toEntity(req);
        entity.setPasswordEncrypted(passwordAes.encryptBase64(req.getPassword()));
        entity.setLastOperatorId(McpUserContextHolder.getUserId());
        mapper.insert(entity);
        return toRes(entity);
    }

    public AdminDataSourceRes update(Long id, AdminDataSourceSaveReq req) {
        McpDataSourceEntity old = mapper.findById(id);
        if (old == null) {
            throw new BusinessException(404, "数据源不存在");
        }
        normalize(req);
        if (!old.getDatasourceKey().equals(req.getDatasourceKey())) {
            throw new BusinessException(400, "数据源Key创建后不能修改");
        }

        McpDataSourceEntity exists = mapper.findByDatasourceKey(req.getDatasourceKey());
        if (exists != null && !exists.getId().equals(id)) {
            throw new BusinessException(400, "数据源Key已存在: " + req.getDatasourceKey());
        }

        McpDataSourceEntity entity = toEntity(req);
        entity.setId(id);
        entity.setDatasourceKey(old.getDatasourceKey());
        String plainPassword = oldPlainPassword(old);
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            plainPassword = req.getPassword();
            entity.setPasswordEncrypted(passwordAes.encryptBase64(req.getPassword()));
        }
        testConnection(req, plainPassword);
        entity.setLastOperatorId(McpUserContextHolder.getUserId());
        mapper.update(entity);
        return toRes(mapper.findById(id));
    }

    public void testConnection(AdminDataSourceSaveReq req) {
        normalize(req);
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new BusinessException(400, "测试连接时密码不能为空");
        }
        testConnection(req, req.getPassword());
    }

    public void testConnection(Long id, AdminDataSourceSaveReq req) {
        McpDataSourceEntity old = mapper.findById(id);
        if (old == null) {
            throw new BusinessException(404, "数据源不存在");
        }
        normalize(req);
        String plainPassword = req.getPassword() != null && !req.getPassword().isBlank()
                ? req.getPassword()
                : oldPlainPassword(old);
        testConnection(req, plainPassword);
    }

    public void delete(Long id) {
        if (mapper.findById(id) == null) {
            throw new BusinessException(404, "数据源不存在");
        }
        mapper.deleteById(id);
    }

    private void normalize(AdminDataSourceSaveReq req) {
        req.setName(trimRequired(req.getName(), "数据源名称不能为空"));
        req.setDatasourceKey(trimRequired(req.getDatasourceKey(), "数据源Key不能为空"));
        req.setDbType(normalizeDbType(req.getDbType()));
        req.setJdbcUrl(trimRequired(req.getJdbcUrl(), "JDBC URL不能为空"));
        req.setUsername(trimRequired(req.getUsername(), "数据库用户名不能为空"));
        validateJdbcUrl(req.getJdbcUrl());
        validateJsonObject(req.getExtraJdbcProps(), "额外 JDBC 参数必须是 JSON 对象");
        if (req.getExtraJdbcProps() == null || req.getExtraJdbcProps().isBlank()) {
            req.setExtraJdbcProps("{}");
        }
        if (req.getPublishStatus() == null) {
            req.setPublishStatus(PUBLISH_DRAFT);
        }
        if (req.getPublishStatus() != PUBLISH_DRAFT && req.getPublishStatus() != PUBLISH_LIVE) {
            throw new BusinessException(400, "数据源发布状态不正确");
        }
    }

    private McpDataSourceEntity toEntity(AdminDataSourceSaveReq req) {
        McpDataSourceEntity entity = new McpDataSourceEntity();
        entity.setName(req.getName());
        entity.setDatasourceKey(req.getDatasourceKey());
        entity.setDbType(req.getDbType());
        entity.setJdbcUrl(req.getJdbcUrl());
        entity.setUsername(req.getUsername());
        entity.setExtraJdbcProps(req.getExtraJdbcProps());
        entity.setDescription(req.getDescription());
        entity.setPublishStatus(req.getPublishStatus());
        return entity;
    }

    private AdminDataSourceRes toRes(McpDataSourceEntity entity) {
        AdminDataSourceRes res = new AdminDataSourceRes();
        res.setId(entity.getId());
        res.setName(entity.getName());
        res.setDatasourceKey(entity.getDatasourceKey());
        res.setDbType(entity.getDbType());
        res.setJdbcUrl(entity.getJdbcUrl());
        res.setUsername(entity.getUsername());
        res.setPasswordSet(entity.getPasswordEncrypted() != null && !entity.getPasswordEncrypted().isBlank());
        res.setExtraJdbcProps(entity.getExtraJdbcProps());
        res.setDescription(entity.getDescription());
        res.setPublishStatus(entity.getPublishStatus());
        res.setLastOperatorId(entity.getLastOperatorId());
        res.setCreateTime(entity.getCreateTime());
        res.setUpdateTime(entity.getUpdateTime());
        return res;
    }

    private String normalizeDbType(String dbType) {
        String value = trimRequired(dbType, "数据库类型不能为空").toUpperCase();
        if (!"MYSQL".equals(value) && !"TIDB".equals(value)) {
            throw new BusinessException(400, "当前课堂版仅支持 MYSQL、TIDB");
        }
        return value;
    }

    private void validateJdbcUrl(String jdbcUrl) {
        if (!jdbcUrl.startsWith("jdbc:mysql:") && !jdbcUrl.startsWith("jdbc:mariadb:")) {
            throw new BusinessException(400, "JDBC URL 必须以 jdbc:mysql: 或 jdbc:mariadb: 开头");
        }
        if (jdbcUrl.startsWith("jdbc:mysql//") || jdbcUrl.startsWith("jdbc:mariadb//")) {
            throw new BusinessException(400, "JDBC URL 格式错误，应为 jdbc:mysql://主机:端口/库名");
        }
    }

    private void validateJsonObject(String json, String message) {
        if (json == null || json.isBlank()) {
            return;
        }
        try {
            objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new BusinessException(400, message);
        }
    }

    private void testConnection(AdminDataSourceSaveReq req, String plainPassword) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Properties props = new Properties();
            props.setProperty("user", req.getUsername());
            props.setProperty("password", plainPassword != null ? plainPassword : "");
            applyExtraJdbcProps(props, req.getExtraJdbcProps());
            try (Connection connection = DriverManager.getConnection(req.getJdbcUrl(), props);
                 var statement = connection.createStatement()) {
                statement.execute("SELECT 1");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "数据源连接失败: " + e.getMessage());
        }
    }

    private void applyExtraJdbcProps(Properties props, String extraJdbcProps) {
        if (extraJdbcProps == null || extraJdbcProps.isBlank()) {
            return;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(extraJdbcProps, new TypeReference<>() {
            });
            map.forEach((key, value) -> {
                if (key != null && value != null) {
                    props.setProperty(key, String.valueOf(value));
                }
            });
        } catch (Exception e) {
            throw new BusinessException(400, "额外 JDBC 参数必须是 JSON 对象");
        }
    }

    private String oldPlainPassword(McpDataSourceEntity old) {
        try {
            return passwordAes.decryptStr(old.getPasswordEncrypted());
        } catch (Exception e) {
            throw new BusinessException(500, "数据源密码解密失败");
        }
    }

    private String trimRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(400, message);
        }
        return value.trim();
    }

    private byte[] aesKey(String secret) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(digest, 16);
        } catch (Exception e) {
            throw new IllegalStateException("初始化数据源密码加密器失败", e);
        }
    }
}
