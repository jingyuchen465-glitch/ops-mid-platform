package com.bear.mcp.single.admin.res;

import lombok.Data;

import java.util.Date;

/** 角色 Resource 权限展示对象。 */
@Data
public class AdminRoleResourceRes {
    private Long id;
    private String roleCode;
    private String resourceUri;
    private Date createTime;
}
