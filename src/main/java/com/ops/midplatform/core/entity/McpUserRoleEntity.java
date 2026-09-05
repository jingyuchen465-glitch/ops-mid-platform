package com.ops.midplatform.core.entity;

import lombok.Data;

import java.util.Date;

/** 对应 mcp_user_role 表。 */
@Data
public class McpUserRoleEntity {
    /** 主键。 */
    private Long id;
    /** 用户 ID。 */
    private Long userId;
    /** 角色编码。 */
    private String roleCode;
    /** 创建时间。 */
    private Date createTime;
}
