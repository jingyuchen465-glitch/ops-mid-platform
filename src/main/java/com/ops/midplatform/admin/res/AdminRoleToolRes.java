package com.ops.midplatform.admin.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class AdminRoleToolRes {
    private Long id;
    private String roleCode;
    private String toolName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
