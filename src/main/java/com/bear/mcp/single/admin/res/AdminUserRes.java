package com.bear.mcp.single.admin.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/** 管理用户对外响应，绝不携带 passwordHash。 */
@Data
public class AdminUserRes {
    private Long id;
    private String username;
    private String displayName;
    private Integer isEnabled;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
