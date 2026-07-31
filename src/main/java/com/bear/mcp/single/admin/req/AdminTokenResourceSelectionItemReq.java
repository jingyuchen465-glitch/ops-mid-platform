package com.bear.mcp.single.admin.req;

import lombok.Data;

/** Token Resource 选择项。 */
@Data
public class AdminTokenResourceSelectionItemReq {
    private String resourceUri;
    private Integer enabled;
}
