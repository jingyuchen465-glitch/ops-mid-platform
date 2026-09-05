package com.ops.midplatform.core.entity;

import lombok.Data;

import java.util.Date;

/** 对应 mcp_role 表。 */
@Data
public class McpRoleEntity {
    /** 角色主键。 */
    private Long id;
    /** 角色编码。 */
    private String roleCode;
    /** 角色名称。 */
    private String roleName;
    /** 角色描述。 */
    private String description;
    /** 是否启用：1-启用，0-禁用。 */
    private Integer isEnabled;
    /** 创建时间。 */
    private Date createTime;
    /** 更新时间。 */
    private Date updateTime;
}
