package com.bear.mcp.single.admin.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 新建或编辑管理用户的请求。 */
@Data
public class AdminUserSaveReq {
    /** 登录用户名。 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 页面展示名称。 */
    @NotBlank(message = "展示名称不能为空")
    private String displayName;

    /** 新建用户时可传入的初始密码，不传则使用课程默认密码。 */
    private String password;

    /** 是否启用：1-启用，0-禁用。 */
    private Integer isEnabled;
}
