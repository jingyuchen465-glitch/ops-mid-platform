package com.bear.mcp.single.share.res;

import lombok.Data;

/** Skill Markdown 上传预签名响应。 */
@Data
public class ShareStudioSkillPresignRes {
    private String uploadUrl;
    private String objectKey;
    private String skillCode;
    private Long expires;
}
