package com.ops.midplatform.admin.res;

import lombok.Data;

import java.util.Date;

/** 角色 Prompt 权限展示对象。 */
@Data
public class AdminRolePromptRes {
    private Long id;
    private String roleCode;
    private String promptName;
    private Date createTime;
}
