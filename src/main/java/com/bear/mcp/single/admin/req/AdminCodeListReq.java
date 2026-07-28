package com.bear.mcp.single.admin.req;

import lombok.Data;
import java.util.List;

/** 批量替换角色、工具关联时使用的请求。 */
@Data
public class AdminCodeListReq {
    private List<String> codes;
}
