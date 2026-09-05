package com.ops.midplatform.share.service;

import com.ops.midplatform.common.exception.BusinessException;
import com.ops.midplatform.core.entity.McpUserRoleEntity;
import com.ops.midplatform.core.groovy.GroovyScriptEngine;
import com.ops.midplatform.core.mapper.McpDataSourceMapper;
import com.ops.midplatform.core.mapper.McpDynamicToolMapper;
import com.ops.midplatform.core.mapper.McpRequestConfigMapper;
import com.ops.midplatform.core.mapper.McpRoleMapper;
import com.ops.midplatform.core.mapper.McpUserRoleMapper;
import com.ops.midplatform.core.redis.RedisPermissionPolicy;
import com.ops.midplatform.share.req.ShareStudioToolSaveReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShareStudioToolServiceTest {

    @Mock
    private McpDynamicToolMapper dynamicToolMapper;
    @Mock
    private McpRequestConfigMapper requestConfigMapper;
    @Mock
    private McpDataSourceMapper dataSourceMapper;
    @Mock
    private GroovyScriptEngine groovyScriptEngine;
    @Mock
    private McpUserRoleMapper userRoleMapper;
    @Mock
    private McpRoleMapper roleMapper;

    private ShareStudioToolService service;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        service = new ShareStudioToolService(
                dynamicToolMapper,
                requestConfigMapper,
                dataSourceMapper,
                groovyScriptEngine,
                objectMapper,
                userRoleMapper,
                roleMapper,
                new RedisPermissionPolicy(objectMapper));
    }

    @Test
    void shouldRejectRedisToolCreationByNonAdmin() {
        McpUserRoleEntity developerRole = new McpUserRoleEntity();
        developerRole.setRoleCode("DEVELOPER");
        when(userRoleMapper.findByUserId(10002L)).thenReturn(List.of(developerRole));

        ShareStudioToolSaveReq request = new ShareStudioToolSaveReq();
        request.setToolName("feishu_test");
        request.setToolDescription("test");
        request.setInputSchema("{\"type\":\"object\",\"properties\":{}}");
        request.setGroovyScript("return [success: true]");
        request.setLinkedRequestKeys("[]");
        request.setLinkedDataSourceIds("[]");
        request.setLinkedRedisPermissions("""
                [{
                  "key":"bear:feishu:app:user:{userId}",
                  "commands":["HMGET"],
                  "fields":["appId","appSecret","enabled"]
                }]
                """);
        request.setEnabled(0);
        request.setPublishStatus(0);

        assertThatThrownBy(() -> service.create(10002L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 403)
                .hasMessageContaining("只有管理员");
        verify(dynamicToolMapper, never()).insert(any());
    }
}
