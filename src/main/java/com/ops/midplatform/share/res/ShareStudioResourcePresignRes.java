package com.ops.midplatform.share.res;

import lombok.Data;

/** Resource Markdown 文件上传预签名响应。 */
@Data
public class ShareStudioResourcePresignRes {
    private String uploadUrl;
    private String objectKey;
    private Long expires;
}
