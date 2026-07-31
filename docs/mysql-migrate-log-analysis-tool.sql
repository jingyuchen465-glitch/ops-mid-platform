-- 接入 YanQue 日志查询接口：cn.yanque.models.loganalysis.controller.LogAnalysisController#search
-- API: query_log_analysis_search
-- Tool: query_log_analysis_search_tool

INSERT INTO mcp_request_config (
    request_id, config_key, name, type, method, url, headers, body_template, params_default,
    connect_timeout_ms, read_timeout_ms, service_name, method_name, args_schema, creator_id,
    is_enabled, rate_limit_per_minute, publish_status, description, category
) VALUES (
    'API_LOG_ANALYSIS_SEARCH',
    'query_log_analysis_search',
    '日志查询',
    'HTTP',
    'GET',
    'http://127.0.0.1:8081/yq-admin/api/log-analysis/search?keyword={{keyword}}&userId={{userId}}&username={{username}}&module={{module}}&action={{action}}&startTime={{startTime}}&endTime={{endTime}}&pageNum={{pageNum}}&pageSize={{pageSize}}',
    JSON_OBJECT(),
    '',
    JSON_OBJECT(
        'keyword', '',
        'userId', '',
        'username', '',
        'module', '',
        'action', '',
        'startTime', '',
        'endTime', '',
        'pageNum', 1,
        'pageSize', 20
    ),
    5000,
    20000,
    NULL,
    NULL,
    JSON_OBJECT(),
    10001,
    1,
    60,
    2,
    '调用 YanQue LogAnalysisController#search 查询系统日志。支持关键词、用户、模块、动作、时间范围和分页筛选。',
    'YanQue 日志'
) ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    type = VALUES(type),
    method = VALUES(method),
    url = VALUES(url),
    headers = VALUES(headers),
    body_template = VALUES(body_template),
    params_default = VALUES(params_default),
    connect_timeout_ms = VALUES(connect_timeout_ms),
    read_timeout_ms = VALUES(read_timeout_ms),
    args_schema = VALUES(args_schema),
    creator_id = VALUES(creator_id),
    is_enabled = VALUES(is_enabled),
    rate_limit_per_minute = VALUES(rate_limit_per_minute),
    publish_status = VALUES(publish_status),
    description = VALUES(description),
    category = VALUES(category);

INSERT INTO mcp_dynamic_tool (
    tool_name, tool_description, input_schema, groovy_script,
    linked_request_keys, linked_data_source_ids, is_enabled, publish_status
) VALUES (
    'query_log_analysis_search_tool',
    '查询 YanQue 系统日志。支持按关键词、用户 ID、用户名、模块、动作、开始/结束时间和分页条件筛选，适合排查用户操作记录、接口调用记录或后台运行日志。',
    JSON_OBJECT(
        'type', 'object',
        'required', JSON_ARRAY(),
        'properties', JSON_OBJECT(
            'keyword', JSON_OBJECT('type', 'string', 'description', '关键词，可匹配日志内容、标题或摘要'),
            'userId', JSON_OBJECT('type', 'integer', 'description', '用户ID'),
            'username', JSON_OBJECT('type', 'string', 'description', '用户名'),
            'module', JSON_OBJECT('type', 'string', 'description', '业务模块或日志模块'),
            'action', JSON_OBJECT('type', 'string', 'description', '操作动作，如 CREATE、UPDATE、DELETE、LOGIN、QUERY 等'),
            'startTime', JSON_OBJECT('type', 'string', 'description', '开始时间，建议格式 yyyy-MM-dd HH:mm:ss'),
            'endTime', JSON_OBJECT('type', 'string', 'description', '结束时间，建议格式 yyyy-MM-dd HH:mm:ss'),
            'pageNum', JSON_OBJECT('type', 'integer', 'description', '页码，默认 1'),
            'pageSize', JSON_OBJECT('type', 'integer', 'description', '每页条数，默认 20')
        ),
        'additionalProperties', false
    ),
    'def requestParams = [
  keyword: params.keyword ?: "",
  userId: params.userId ?: "",
  username: params.username ?: "",
  module: params.module ?: "",
  action: params.action ?: "",
  startTime: params.startTime ?: "",
  endTime: params.endTime ?: "",
  pageNum: params.pageNum ?: 1,
  pageSize: params.pageSize ?: 20
]
def response = runRequest.runRequest("query_log_analysis_search", requestParams)
return [
  success: true,
  query: requestParams,
  response: response
]',
    JSON_ARRAY('query_log_analysis_search'),
    JSON_ARRAY(),
    1,
    2
) ON DUPLICATE KEY UPDATE
    tool_description = VALUES(tool_description),
    input_schema = VALUES(input_schema),
    groovy_script = VALUES(groovy_script),
    linked_request_keys = VALUES(linked_request_keys),
    linked_data_source_ids = VALUES(linked_data_source_ids),
    is_enabled = VALUES(is_enabled),
    publish_status = VALUES(publish_status);

INSERT IGNORE INTO mcp_role_tool (role_code, tool_name) VALUES
('ADMIN', 'query_log_analysis_search_tool');

INSERT INTO mcp_user_tool_selection (token_id, tool_name, tool_type, is_enabled) VALUES
(1, 'query_log_analysis_search_tool', 'DYNAMIC', 1)
ON DUPLICATE KEY UPDATE
    tool_type = VALUES(tool_type),
    is_enabled = VALUES(is_enabled);
