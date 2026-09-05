package com.bear.mcp.single.share.req;

import lombok.Data;

import java.util.Map;

/** 创作空间 Prompt 调试请求。 */
@Data
public class ShareStudioPromptDebugReq {
    /** 未保存时传入完整 Prompt 配置。 */
    private ShareStudioPromptSaveReq prompt;

    /** 本次调试传入的模板参数。 */
    private Map<String, Object> arguments;
}
