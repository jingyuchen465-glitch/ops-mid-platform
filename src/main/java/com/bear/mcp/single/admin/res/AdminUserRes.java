package com.bear.mcp.single.admin.res;

import lombok.Data;
import java.util.Date;

/** 管理用户对外响应，绝不携带 passwordHash。 */
@Data
public class AdminUserRes {
    private Long id;
    private String username;
    private String displayName;
    private Integer isEnabled;
    private Date createTime;
    private Date updateTime;
}
