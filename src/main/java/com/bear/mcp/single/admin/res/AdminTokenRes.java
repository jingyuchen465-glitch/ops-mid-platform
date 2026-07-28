package com.bear.mcp.single.admin.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/** 管理端 Token 返回对象。 */
@Data
public class AdminTokenRes {
    /** Token 主键。 */
    private Long id;

    /** Token 所属用户 ID。 */
    private Long userId;

    /** Token 名称。 */
    private String tokenName;

    /** 用于展示的 Token 前缀。 */
    private String tokenPrefix;

    /** Token 权限范围 JSON。 */
    private String permissions;

    /** 过期时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /** 最后使用时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUsedTime;

    /** 最后使用 IP。 */
    private String lastUsedIp;

    /** 是否启用：1-启用，0-禁用。 */
    private Integer isActive;

    /** 创建时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
