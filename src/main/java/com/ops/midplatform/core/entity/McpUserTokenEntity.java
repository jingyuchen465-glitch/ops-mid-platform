package com.ops.midplatform.core.entity;

import lombok.Data;

import java.util.Date;

/** 对应 mcp_user_token 表。 */
@Data
public class McpUserTokenEntity {
    /** Token 主键。 */
    private Long id;
    /** Token 所属用户 ID。 */
    private Long userId;
    /** Token 名称。 */
    private String tokenName;
    /** Token 的 SHA-256 哈希值。 */
    private String tokenHash;
    /** AES 加密后的 Token，仅管理后台需要时使用。 */
    private String tokenEncrypted;
    /** 用于展示的 Token 前缀。 */
    private String tokenPrefix;
    /** Token 权限范围 JSON。 */
    private String permissions;
    /** 过期时间，NULL 表示不过期。 */
    private Date expireTime;
    /** 最后使用时间。 */
    private Date lastUsedTime;
    /** 最后使用 IP。 */
    private String lastUsedIp;
    /** 是否有效：1-有效，0-禁用。 */
    private Integer isActive;
    /** 创建时间。 */
    private Date createTime;
}
