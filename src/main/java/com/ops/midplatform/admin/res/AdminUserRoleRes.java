package com.ops.midplatform.admin.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/** 用户角色关系响应，用于管理端分配角色弹窗。 */
@Data
public class AdminUserRoleRes {
    /** 关系主键。 */
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 角色编码。 */
    private String roleCode;

    /** 分配时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
