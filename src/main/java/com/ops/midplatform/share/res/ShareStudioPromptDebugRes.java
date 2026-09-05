package com.bear.mcp.single.share.res;

import lombok.Data;

import java.util.Map;

/** Prompt 模板调试响应。 */
@Data
public class ShareStudioPromptDebugRes {
    private Boolean success;
    private String renderedContent;
    private Map<String, Object> arguments;
    private String errorMessage;
}
