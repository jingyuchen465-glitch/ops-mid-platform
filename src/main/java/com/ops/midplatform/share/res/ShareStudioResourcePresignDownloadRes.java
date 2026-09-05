package com.ops.midplatform.share.res;

import lombok.Data;

/** Resource Markdown 文件下载预签名响应。 */
@Data
public class ShareStudioResourcePresignDownloadRes {
    private String downloadUrl;
    private Long expires;
}
