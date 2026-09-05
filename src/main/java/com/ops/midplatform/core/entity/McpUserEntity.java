package com.ops.midplatform.core.entity;

import lombok.Data;

import java.util.Date;

/** 对应 mcp_user 表。 */
@Data
public class McpUserEntity {
    /** 用户主键。 */
    private Long id;
    /** 登录用户名。 */
    private String username;
    /** BCrypt 加密后的管理端登录密码。 */
    private String passwordHash;
    /** 展示名称。 */
    private String displayName;
    /** 是否启用：1-启用，0-禁用。 */
    private Integer isEnabled;
    /** 创建时间。 */
    private Date createTime;
    /** 更新时间。 */
    private Date updateTime;
}
