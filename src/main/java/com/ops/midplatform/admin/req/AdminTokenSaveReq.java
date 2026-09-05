package com.ops.midplatform.admin.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/** 管理端保存 Token 的请求参数。 */
@Data
public class AdminTokenSaveReq {
    /** Token 所属用户 ID。 */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** Token 名称。 */
    @NotBlank(message = "Token名称不能为空")
    private String tokenName;

    /** Token 权限范围 JSON。 */
    private String permissions;

    /** 过期时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /** 是否启用：1-启用，0-禁用。 */
    private Integer isActive;
}
