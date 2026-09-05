package com.ops.midplatform.admin.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 管理端保存数据源配置的请求参数。 */
@Data
public class AdminDataSourceSaveReq {
    @NotBlank(message = "数据源名称不能为空")
    private String name;

    @NotBlank(message = "数据源Key不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "数据源Key只能包含字母、数字、下划线")
    private String datasourceKey;

    @NotBlank(message = "数据库类型不能为空")
    private String dbType;

    @NotBlank(message = "JDBC URL不能为空")
    private String jdbcUrl;

    @NotBlank(message = "数据库用户名不能为空")
    private String username;

    /** 新建必填；编辑时留空表示不修改密码。 */
    private String password;

    private String extraJdbcProps;

    private String description;

    /** 发布状态：0-草稿，1-已发布。 */
    private Integer publishStatus;
}
