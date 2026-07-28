package com.bear.mcp.single.admin.res;

import lombok.Data;

/** 管理员登录成功响应。 */
@Data
public class AdminLoginRes {
    /** JWT，前端后续通过 Authorization: Bearer 传递。 */
    private String token;
    private Long userId;
    private String username;
    private String displayName;
}
