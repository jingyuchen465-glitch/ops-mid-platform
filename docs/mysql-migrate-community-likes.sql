CREATE TABLE IF NOT EXISTS mcp_community_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    target_type VARCHAR(32) NOT NULL COMMENT '点赞对象类型：SKILL、PROMPT、RESOURCE、DYNAMIC_TOOL、BUILTIN_TOOL',
    target_key VARCHAR(128) NOT NULL COMMENT '点赞对象键：数据库 ID 或内置 Tool 名称',
    user_id BIGINT NOT NULL COMMENT '点赞用户 ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_mcp_community_like_user_target (user_id, target_type, target_key),
    KEY idx_mcp_community_like_target (target_type, target_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区点赞表';
