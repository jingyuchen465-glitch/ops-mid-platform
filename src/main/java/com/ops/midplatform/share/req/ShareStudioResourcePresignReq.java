package com.bear.mcp.single.share.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Resource Markdown 文件上传预签名请求。 */
@Data
public class ShareStudioResourcePresignReq {
    @NotBlank(message = "Resource URI不能为空")
    private String resourceUri;

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    private Long fileSize;
}
