package com.bear.mcp.single.admin.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 新建或编辑角色的请求。 */
@Data
public class AdminRoleSaveReq {
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    private String description;
    private Integer isEnabled;
}
