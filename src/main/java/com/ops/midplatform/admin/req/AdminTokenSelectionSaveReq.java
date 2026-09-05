package com.bear.mcp.single.admin.req;

import lombok.Data;

import java.util.List;

/** 管理端保存 Token 工具选择的请求参数。 */
@Data
public class AdminTokenSelectionSaveReq {
    /** 这把 Token 实际加载、展示的工具列表。 */
    private List<AdminTokenSelectionItemReq> tools;
}
