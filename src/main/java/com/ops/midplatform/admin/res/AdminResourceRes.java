package com.bear.mcp.single.admin.res;

import lombok.Data;

/** 后台资源选项展示对象。 */
@Data
public class AdminResourceRes {
    private Long id;
    private String resourceUri;
    private String name;
    private String description;
    private String mimeType;
    private Integer enabled;
    private Integer publishStatus;
}
