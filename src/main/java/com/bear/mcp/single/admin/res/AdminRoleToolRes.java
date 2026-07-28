package com.bear.mcp.single.admin.res;

import lombok.Data;
import java.util.Date;

@Data
public class AdminRoleToolRes {
    private Long id;
    private String roleCode;
    private String toolName;
    private Date createTime;
}
