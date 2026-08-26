-- Feishu tools implemented as dynamic Groovy tools.
-- Prerequisite: mysql-migrate-dynamic-tool-redis.sql has been applied.

INSERT INTO mcp_request_config (
    request_id, config_key, name, type, method, url, headers, body_template, params_default,
    connect_timeout_ms, read_timeout_ms, service_name, method_name, args_schema, creator_id,
    is_enabled, rate_limit_per_minute, publish_status, description, category
) VALUES
(
    'API0000000101', 'feishu_get_tenant_token', '飞书获取 tenant_access_token', 'HTTP', 'POST',
    'https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal',
    JSON_OBJECT('Content-Type', 'application/json; charset=utf-8'), '{{bodyJson}}', JSON_OBJECT(),
    5000, 15000, NULL, NULL, JSON_OBJECT(), NULL, 1, 120, 1,
    '使用用户绑定的 appId 和 appSecret 获取飞书 tenant_access_token', '飞书'
),
(
    'API0000000102', 'feishu_send_message_api', '飞书发送消息 API', 'HTTP', 'POST',
    'https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type={{receiveIdType}}',
    JSON_OBJECT('Authorization', 'Bearer {{tenantAccessToken}}', 'Content-Type', 'application/json; charset=utf-8'),
    '{{bodyJson}}', JSON_OBJECT(), 5000, 15000, NULL, NULL, JSON_OBJECT(), NULL, 1, 600, 1,
    '发送飞书消息', '飞书'
),
(
    'API0000000103', 'feishu_read_message_api', '飞书读取消息 API', 'HTTP', 'GET',
    'https://open.feishu.cn/open-apis/im/v1/messages/{{messageId}}',
    JSON_OBJECT('Authorization', 'Bearer {{tenantAccessToken}}'), '', JSON_OBJECT(),
    5000, 15000, NULL, NULL, JSON_OBJECT(), NULL, 1, 600, 1, '读取飞书消息详情', '飞书'
),
(
    'API0000000104', 'feishu_read_document_api', '飞书读取云文档 API', 'HTTP', 'GET',
    'https://open.feishu.cn/open-apis/docx/v1/documents/{{documentId}}/raw_content',
    JSON_OBJECT('Authorization', 'Bearer {{tenantAccessToken}}'), '', JSON_OBJECT(),
    5000, 15000, NULL, NULL, JSON_OBJECT(), NULL, 1, 600, 1, '读取飞书新版云文档纯文本', '飞书'
),
(
    'API0000000105', 'feishu_create_document_api', '飞书创建云文档 API', 'HTTP', 'POST',
    'https://open.feishu.cn/open-apis/docx/v1/documents',
    JSON_OBJECT('Authorization', 'Bearer {{tenantAccessToken}}', 'Content-Type', 'application/json; charset=utf-8'),
    '{{bodyJson}}', JSON_OBJECT(), 5000, 15000, NULL, NULL, JSON_OBJECT(), NULL, 1, 600, 1,
    '创建飞书新版云文档', '飞书'
),
(
    'API0000000106', 'feishu_read_sheet_api', '飞书读取电子表格 API', 'HTTP', 'GET',
    'https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/{{spreadsheetToken}}/values/{{range}}',
    JSON_OBJECT('Authorization', 'Bearer {{tenantAccessToken}}'), '', JSON_OBJECT(),
    5000, 15000, NULL, NULL, JSON_OBJECT(), NULL, 1, 600, 1, '读取飞书电子表格指定范围', '飞书'
),
(
    'API0000000107', 'feishu_write_sheet_api', '飞书写入电子表格 API', 'HTTP', 'PUT',
    'https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/{{spreadsheetToken}}/values',
    JSON_OBJECT('Authorization', 'Bearer {{tenantAccessToken}}', 'Content-Type', 'application/json; charset=utf-8'),
    '{{bodyJson}}', JSON_OBJECT(), 5000, 15000, NULL, NULL, JSON_OBJECT(), NULL, 1, 600, 1,
    '写入飞书电子表格指定范围', '飞书'
)
ON DUPLICATE KEY UPDATE
    name = VALUES(name), type = VALUES(type), method = VALUES(method), url = VALUES(url),
    headers = VALUES(headers), body_template = VALUES(body_template), params_default = VALUES(params_default),
    connect_timeout_ms = VALUES(connect_timeout_ms), read_timeout_ms = VALUES(read_timeout_ms),
    is_enabled = VALUES(is_enabled), rate_limit_per_minute = VALUES(rate_limit_per_minute),
    publish_status = VALUES(publish_status), description = VALUES(description), category = VALUES(category);

-- Shared script prefix: resolve per-user credentials and maintain the tenant token in Redis.
SET @feishu_common = 'def failure = { message -> [success: false, message: String.valueOf(message)] }
try {
    def requireText = { value, label ->
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException(label + "不能为空")
        }
        String.valueOf(value).trim()
    }
    def encode = { value -> java.net.URLEncoder.encode(String.valueOf(value), "UTF-8") }
    def parseResponse = { response ->
        int status = ((Number) response.status).intValue()
        if (status < 200 || status >= 300) {
            throw new IllegalStateException("飞书HTTP请求失败，status=" + status)
        }
        String bodyText = response.body != null ? String.valueOf(response.body) : ""
        def parsed = bodyText.isBlank() ? [:] : new JsonSlurper().parseText(bodyText)
        if (parsed.code != null && ((Number) parsed.code).intValue() != 0) {
            throw new IllegalStateException("飞书接口返回错误 code=" + parsed.code + ", msg=" + parsed.msg)
        }
        parsed
    }

    String credentialKey = "bear:feishu:app:user:${userId}"
    def credentials = runRedis.hmget(credentialKey, ["appId", "appSecret", "enabled"])
    if (!(credentials.enabled == "1" || String.valueOf(credentials.enabled).equalsIgnoreCase("true"))) {
        throw new IllegalStateException("当前用户的飞书应用未启用")
    }
    String appId = requireText(credentials.appId, "飞书appId")
    String appSecret = requireText(credentials.appSecret, "飞书appSecret")
    String tokenKey = "bear:feishu:tenant-token:user:${userId}"
    String tenantAccessToken = null
    String cachedTokenJson = runRedis.get(tokenKey)
    if (cachedTokenJson != null && !cachedTokenJson.isBlank()) {
        try {
            def cachedToken = new JsonSlurper().parseText(cachedTokenJson)
            if (String.valueOf(cachedToken.appId) == appId && cachedToken.tenantAccessToken != null
                    && !String.valueOf(cachedToken.tenantAccessToken).isBlank()) {
                tenantAccessToken = String.valueOf(cachedToken.tenantAccessToken)
            }
        } catch (Exception ignored) {
            tenantAccessToken = null
        }
    }
    if (tenantAccessToken == null) {
        def tokenResponse = runRequest.runRequest("feishu_get_tenant_token", [
            bodyJson: JsonOutput.toJson([app_id: appId, app_secret: appSecret])
        ])
        def tokenBody = parseResponse(tokenResponse)
        tenantAccessToken = requireText(tokenBody.tenant_access_token, "飞书tenant_access_token")
        long expireSeconds = tokenBody.expire instanceof Number
                ? ((Number) tokenBody.expire).longValue() : 7200L
        long ttlSeconds = Math.min(7200L, Math.max(60L, expireSeconds - 60L))
        runRedis.setEx(tokenKey, JsonOutput.toJson([
            appId: appId,
            tenantAccessToken: tenantAccessToken
        ]), ttlSeconds)
    }
    def callFeishu = { configKey, requestParams ->
        def finalParams = new LinkedHashMap(requestParams != null ? requestParams : [:])
        finalParams.tenantAccessToken = tenantAccessToken
        parseResponse(runRequest.runRequest(configKey, finalParams))
    }
';

SET @feishu_tail = '
} catch (Exception exception) {
    return failure(exception.message)
}';

INSERT INTO mcp_dynamic_tool (
    tool_name, tool_description, input_schema, groovy_script, linked_request_keys,
    linked_data_source_ids, linked_redis_permissions, is_enabled, publish_status
) VALUES (
    'feishu_send_message',
    '通过当前 MCP 用户绑定的飞书应用发送消息。',
    JSON_OBJECT(
        'type', 'object',
        'properties', JSON_OBJECT(
            'receiveId', JSON_OBJECT('type', 'string', 'description', '接收者 ID'),
            'receiveIdType', JSON_OBJECT('type', 'string', 'description', 'open_id、user_id、email 或 chat_id'),
            'msgType', JSON_OBJECT('type', 'string', 'description', 'text、post 或 interactive'),
            'content', JSON_OBJECT('type', 'string', 'description', '消息内容')
        ),
        'required', JSON_ARRAY('receiveId', 'content')
    ),
    CONCAT(@feishu_common, '
    String receiveId = requireText(params.receiveId, "receiveId")
    String receiveIdType = params.receiveIdType == null || String.valueOf(params.receiveIdType).isBlank()
            ? "open_id" : String.valueOf(params.receiveIdType).trim()
    if (!["open_id", "user_id", "email", "chat_id"].contains(receiveIdType)) {
        throw new IllegalArgumentException("receiveIdType不支持: " + receiveIdType)
    }
    String msgType = params.msgType == null || String.valueOf(params.msgType).isBlank()
            ? "text" : String.valueOf(params.msgType).trim().toLowerCase()
    if (!["text", "post", "interactive"].contains(msgType)) {
        throw new IllegalArgumentException("msgType不支持: " + msgType)
    }
    String content = requireText(params.content, "content")
    String feishuContent = msgType == "text" ? JsonOutput.toJson([text: content]) : content
    def bodyJson = JsonOutput.toJson([
        receive_id: receiveId,
        msg_type: msgType,
        content: feishuContent
    ])
    def response = callFeishu("feishu_send_message_api", [
        receiveIdType: receiveIdType,
        bodyJson: bodyJson
    ])
    def data = response.data instanceof Map ? response.data : [:]
    return [success: true, message: "消息发送成功", message_id: data.message_id,
            receive_id_type: receiveIdType]
', @feishu_tail),
    JSON_ARRAY('feishu_get_tenant_token', 'feishu_send_message_api'),
    JSON_ARRAY(),
    JSON_ARRAY(
        JSON_OBJECT('key', 'bear:feishu:app:user:{userId}', 'commands', JSON_ARRAY('HMGET'),
                    'fields', JSON_ARRAY('appId', 'appSecret', 'enabled')),
        JSON_OBJECT('key', 'bear:feishu:tenant-token:user:{userId}', 'commands', JSON_ARRAY('GET', 'SETEX'),
                    'maxTtlSeconds', 7200)
    ),
    1, 1
)
ON DUPLICATE KEY UPDATE
    tool_description = VALUES(tool_description), input_schema = VALUES(input_schema),
    groovy_script = VALUES(groovy_script), linked_request_keys = VALUES(linked_request_keys),
    linked_data_source_ids = VALUES(linked_data_source_ids),
    linked_redis_permissions = VALUES(linked_redis_permissions), is_enabled = 1, publish_status = 1;

INSERT INTO mcp_dynamic_tool (
    tool_name, tool_description, input_schema, groovy_script, linked_request_keys,
    linked_data_source_ids, linked_redis_permissions, is_enabled, publish_status
) VALUES (
    'feishu_read_message', '读取当前 MCP 用户绑定飞书应用中的指定消息。',
    JSON_OBJECT('type', 'object', 'properties', JSON_OBJECT(
        'messageId', JSON_OBJECT('type', 'string', 'description', '飞书消息 ID')
    ), 'required', JSON_ARRAY('messageId')),
    CONCAT(@feishu_common, '
    String messageId = requireText(params.messageId, "messageId")
    def response = callFeishu("feishu_read_message_api", [messageId: encode(messageId)])
    def data = response.data instanceof Map ? response.data : [:]
    return [success: true, message: "查询成功", items: data.items]
', @feishu_tail),
    JSON_ARRAY('feishu_get_tenant_token', 'feishu_read_message_api'), JSON_ARRAY(),
    JSON_ARRAY(
        JSON_OBJECT('key', 'bear:feishu:app:user:{userId}', 'commands', JSON_ARRAY('HMGET'),
                    'fields', JSON_ARRAY('appId', 'appSecret', 'enabled')),
        JSON_OBJECT('key', 'bear:feishu:tenant-token:user:{userId}', 'commands', JSON_ARRAY('GET', 'SETEX'),
                    'maxTtlSeconds', 7200)
    ), 1, 1
)
ON DUPLICATE KEY UPDATE
    tool_description = VALUES(tool_description), input_schema = VALUES(input_schema),
    groovy_script = VALUES(groovy_script), linked_request_keys = VALUES(linked_request_keys),
    linked_data_source_ids = VALUES(linked_data_source_ids),
    linked_redis_permissions = VALUES(linked_redis_permissions), is_enabled = 1, publish_status = 1;

INSERT INTO mcp_dynamic_tool (
    tool_name, tool_description, input_schema, groovy_script, linked_request_keys,
    linked_data_source_ids, linked_redis_permissions, is_enabled, publish_status
) VALUES (
    'feishu_read_document', '读取当前 MCP 用户绑定飞书应用中的新版云文档纯文本。',
    JSON_OBJECT('type', 'object', 'properties', JSON_OBJECT(
        'documentId', JSON_OBJECT('type', 'string', 'description', '飞书文档 ID')
    ), 'required', JSON_ARRAY('documentId')),
    CONCAT(@feishu_common, '
    String documentId = requireText(params.documentId, "documentId")
    def response = callFeishu("feishu_read_document_api", [documentId: encode(documentId)])
    def data = response.data instanceof Map ? response.data : [:]
    return [success: true, message: "读取成功", content: data.content]
', @feishu_tail),
    JSON_ARRAY('feishu_get_tenant_token', 'feishu_read_document_api'), JSON_ARRAY(),
    JSON_ARRAY(
        JSON_OBJECT('key', 'bear:feishu:app:user:{userId}', 'commands', JSON_ARRAY('HMGET'),
                    'fields', JSON_ARRAY('appId', 'appSecret', 'enabled')),
        JSON_OBJECT('key', 'bear:feishu:tenant-token:user:{userId}', 'commands', JSON_ARRAY('GET', 'SETEX'),
                    'maxTtlSeconds', 7200)
    ), 1, 1
)
ON DUPLICATE KEY UPDATE
    tool_description = VALUES(tool_description), input_schema = VALUES(input_schema),
    groovy_script = VALUES(groovy_script), linked_request_keys = VALUES(linked_request_keys),
    linked_data_source_ids = VALUES(linked_data_source_ids),
    linked_redis_permissions = VALUES(linked_redis_permissions), is_enabled = 1, publish_status = 1;

INSERT INTO mcp_dynamic_tool (
    tool_name, tool_description, input_schema, groovy_script, linked_request_keys,
    linked_data_source_ids, linked_redis_permissions, is_enabled, publish_status
) VALUES (
    'feishu_create_document', '通过当前 MCP 用户绑定的飞书应用创建新版云文档。',
    JSON_OBJECT('type', 'object', 'properties', JSON_OBJECT(
        'title', JSON_OBJECT('type', 'string', 'description', '文档标题'),
        'folderToken', JSON_OBJECT('type', 'string', 'description', '可选文件夹 token')
    ), 'required', JSON_ARRAY('title')),
    CONCAT(@feishu_common, '
    String title = requireText(params.title, "title")
    def requestBody = [title: title]
    if (params.folderToken != null && !String.valueOf(params.folderToken).isBlank()) {
        requestBody.folder_token = String.valueOf(params.folderToken).trim()
    }
    def response = callFeishu("feishu_create_document_api", [bodyJson: JsonOutput.toJson(requestBody)])
    def data = response.data instanceof Map ? response.data : [:]
    def document = data.document instanceof Map ? data.document : [:]
    def documentId = document.document_id
    return [success: true, message: "文档创建成功", document_id: documentId,
            url: documentId != null ? "https://feishu.cn/docx/${documentId}" : null]
', @feishu_tail),
    JSON_ARRAY('feishu_get_tenant_token', 'feishu_create_document_api'), JSON_ARRAY(),
    JSON_ARRAY(
        JSON_OBJECT('key', 'bear:feishu:app:user:{userId}', 'commands', JSON_ARRAY('HMGET'),
                    'fields', JSON_ARRAY('appId', 'appSecret', 'enabled')),
        JSON_OBJECT('key', 'bear:feishu:tenant-token:user:{userId}', 'commands', JSON_ARRAY('GET', 'SETEX'),
                    'maxTtlSeconds', 7200)
    ), 1, 1
)
ON DUPLICATE KEY UPDATE
    tool_description = VALUES(tool_description), input_schema = VALUES(input_schema),
    groovy_script = VALUES(groovy_script), linked_request_keys = VALUES(linked_request_keys),
    linked_data_source_ids = VALUES(linked_data_source_ids),
    linked_redis_permissions = VALUES(linked_redis_permissions), is_enabled = 1, publish_status = 1;

INSERT INTO mcp_dynamic_tool (
    tool_name, tool_description, input_schema, groovy_script, linked_request_keys,
    linked_data_source_ids, linked_redis_permissions, is_enabled, publish_status
) VALUES (
    'feishu_read_sheet', '读取当前 MCP 用户绑定飞书应用中的电子表格范围。',
    JSON_OBJECT('type', 'object', 'properties', JSON_OBJECT(
        'spreadsheetToken', JSON_OBJECT('type', 'string', 'description', '电子表格 token'),
        'range', JSON_OBJECT('type', 'string', 'description', '读取范围，如 Sheet1!A1:C10')
    ), 'required', JSON_ARRAY('spreadsheetToken', 'range')),
    CONCAT(@feishu_common, '
    String spreadsheetToken = requireText(params.spreadsheetToken, "spreadsheetToken")
    String range = requireText(params.range, "range")
    def response = callFeishu("feishu_read_sheet_api", [
        spreadsheetToken: encode(spreadsheetToken),
        range: encode(range)
    ])
    def data = response.data instanceof Map ? response.data : [:]
    def valueRange = data.valueRange instanceof Map ? data.valueRange : [:]
    return [success: true, message: "读取成功", range: range, values: valueRange.values]
', @feishu_tail),
    JSON_ARRAY('feishu_get_tenant_token', 'feishu_read_sheet_api'), JSON_ARRAY(),
    JSON_ARRAY(
        JSON_OBJECT('key', 'bear:feishu:app:user:{userId}', 'commands', JSON_ARRAY('HMGET'),
                    'fields', JSON_ARRAY('appId', 'appSecret', 'enabled')),
        JSON_OBJECT('key', 'bear:feishu:tenant-token:user:{userId}', 'commands', JSON_ARRAY('GET', 'SETEX'),
                    'maxTtlSeconds', 7200)
    ), 1, 1
)
ON DUPLICATE KEY UPDATE
    tool_description = VALUES(tool_description), input_schema = VALUES(input_schema),
    groovy_script = VALUES(groovy_script), linked_request_keys = VALUES(linked_request_keys),
    linked_data_source_ids = VALUES(linked_data_source_ids),
    linked_redis_permissions = VALUES(linked_redis_permissions), is_enabled = 1, publish_status = 1;

INSERT INTO mcp_dynamic_tool (
    tool_name, tool_description, input_schema, groovy_script, linked_request_keys,
    linked_data_source_ids, linked_redis_permissions, is_enabled, publish_status
) VALUES (
    'feishu_write_sheet', '向当前 MCP 用户绑定飞书应用中的电子表格写入二维数组。',
    JSON_OBJECT('type', 'object', 'properties', JSON_OBJECT(
        'spreadsheetToken', JSON_OBJECT('type', 'string', 'description', '电子表格 token'),
        'range', JSON_OBJECT('type', 'string', 'description', '写入范围，如 Sheet1!A1:B2'),
        'values', JSON_OBJECT('type', 'string', 'description', 'JSON 二维数组字符串')
    ), 'required', JSON_ARRAY('spreadsheetToken', 'range', 'values')),
    CONCAT(@feishu_common, '
    String spreadsheetToken = requireText(params.spreadsheetToken, "spreadsheetToken")
    String range = requireText(params.range, "range")
    String valuesText = requireText(params.values, "values")
    def parsedValues = new JsonSlurper().parseText(valuesText)
    if (!(parsedValues instanceof List)) {
        throw new IllegalArgumentException("values必须是JSON二维数组")
    }
    def bodyJson = JsonOutput.toJson([valueRange: [range: range, values: parsedValues]])
    def response = callFeishu("feishu_write_sheet_api", [
        spreadsheetToken: encode(spreadsheetToken),
        bodyJson: bodyJson
    ])
    def data = response.data instanceof Map ? response.data : [:]
    return [success: true, message: "写入成功", updated_rows: data.updatedRows,
            updated_columns: data.updatedColumns]
', @feishu_tail),
    JSON_ARRAY('feishu_get_tenant_token', 'feishu_write_sheet_api'), JSON_ARRAY(),
    JSON_ARRAY(
        JSON_OBJECT('key', 'bear:feishu:app:user:{userId}', 'commands', JSON_ARRAY('HMGET'),
                    'fields', JSON_ARRAY('appId', 'appSecret', 'enabled')),
        JSON_OBJECT('key', 'bear:feishu:tenant-token:user:{userId}', 'commands', JSON_ARRAY('GET', 'SETEX'),
                    'maxTtlSeconds', 7200)
    ), 1, 1
)
ON DUPLICATE KEY UPDATE
    tool_description = VALUES(tool_description), input_schema = VALUES(input_schema),
    groovy_script = VALUES(groovy_script), linked_request_keys = VALUES(linked_request_keys),
    linked_data_source_ids = VALUES(linked_data_source_ids),
    linked_redis_permissions = VALUES(linked_redis_permissions), is_enabled = 1, publish_status = 1;

INSERT IGNORE INTO mcp_role_tool (role_code, tool_name) VALUES
('ADMIN', 'feishu_send_message'),
('ADMIN', 'feishu_read_message'),
('ADMIN', 'feishu_read_document'),
('ADMIN', 'feishu_create_document'),
('ADMIN', 'feishu_read_sheet'),
('ADMIN', 'feishu_write_sheet');

UPDATE mcp_user_tool_selection
SET tool_type = 'DYNAMIC', is_enabled = 1
WHERE token_id = 1
  AND tool_name IN (
      'feishu_send_message', 'feishu_read_message', 'feishu_read_document',
      'feishu_create_document', 'feishu_read_sheet', 'feishu_write_sheet'
  );

INSERT INTO mcp_user_tool_selection (token_id, tool_name, tool_type, is_enabled)
SELECT 1, tool_name, 'DYNAMIC', 1
FROM (
    SELECT 'feishu_send_message' AS tool_name
    UNION ALL SELECT 'feishu_read_message'
    UNION ALL SELECT 'feishu_read_document'
    UNION ALL SELECT 'feishu_create_document'
    UNION ALL SELECT 'feishu_read_sheet'
    UNION ALL SELECT 'feishu_write_sheet'
) tools
WHERE NOT EXISTS (
    SELECT 1
    FROM mcp_user_tool_selection selection
    WHERE selection.token_id = 1 AND selection.tool_name = tools.tool_name
);

SET @feishu_common = NULL;
SET @feishu_tail = NULL;
