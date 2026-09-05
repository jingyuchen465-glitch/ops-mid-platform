package com.bear.mcp.single.core.entity;

import lombok.Data;

import java.util.Date;

/** 对应 mcp_data_source 表，保存外部数据库连接配置。 */
@Data
public class McpDataSourceEntity {
    /** 主键。 */
    private Long id;
    /** 数据源名称。 */
    private String name;
    /** 内部唯一 key，后续也可用于脚本或页面定位。 */
    private String datasourceKey;
    /** 数据库类型，当前课堂版先支持 MYSQL、TIDB。 */
    private String dbType;
    /** JDBC URL。 */
    private String jdbcUrl;
    /** 数据库用户名。 */
    private String username;
    /** AES 加密后的数据库密码。 */
    private String passwordEncrypted;
    /** 额外 JDBC 参数 JSON。 */
    private String extraJdbcProps;
    /** 数据源说明。 */
    private String description;
    /** 发布状态：0-草稿，1-已发布。 */
    private Integer publishStatus;
    /** 最近操作人用户 ID。 */
    private Long lastOperatorId;
    /** 创建时间。 */
    private Date createTime;
    /** 更新时间。 */
    private Date updateTime;
}
