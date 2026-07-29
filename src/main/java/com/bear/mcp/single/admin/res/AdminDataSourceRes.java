package com.bear.mcp.single.admin.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/** 管理端数据源配置返回对象，不包含数据库密码密文或明文。 */
@Data
public class AdminDataSourceRes {
    private Long id;
    private String name;
    private String datasourceKey;
    private String dbType;
    private String jdbcUrl;
    private String username;
    private Boolean passwordSet;
    private String extraJdbcProps;
    private String description;
    private Integer publishStatus;
    private Long lastOperatorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
