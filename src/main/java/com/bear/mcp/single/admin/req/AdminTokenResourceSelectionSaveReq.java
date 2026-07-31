package com.bear.mcp.single.admin.req;

import lombok.Data;

import java.util.List;

/** Token Resource 选择保存请求。 */
@Data
public class AdminTokenResourceSelectionSaveReq {
    private List<AdminTokenResourceSelectionItemReq> resources;
}
