package com.bear.mcp.single.share.res;

import lombok.Data;

/** Skill Markdown 下载预签名响应。 */
@Data
public class ShareStudioSkillPresignDownloadRes {
    private String downloadUrl;
    private Long expires;
}
