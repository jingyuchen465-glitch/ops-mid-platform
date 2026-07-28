package com.bear.mcp.single.admin.res;

import lombok.Data;
import java.util.Date;

@Data
public class AdminRoleRes {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private Integer isEnabled;
    private Date createTime;
    private Date updateTime;
}
