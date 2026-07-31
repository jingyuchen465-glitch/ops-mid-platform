package com.bear.mcp.single.share.res;

import lombok.Data;

/** Cursor MCP 一键安装链接响应。 */
@Data
public class ShareCursorInstallRes {
    private String serverName;
    private String deeplink;
    private String configJson;
}
