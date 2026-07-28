package com.bear.mcp.single.admin.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 管理员登录请求。 */
@Data
public class AdminLoginReq {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
}
