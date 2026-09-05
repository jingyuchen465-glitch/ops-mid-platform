package com.ops.midplatform.share.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 创作空间保存 Resource 请求。 */
@Data
public class ShareStudioResourceSaveReq {
    @NotBlank(message = "Resource URI不能为空")
    private String resourceUri;

    @NotBlank(message = "资源名称不能为空")
    private String name;

    private String description;
    private String mimeType;

    @NotBlank(message = "请先上传 Markdown 文件")
    private String objectKey;

    private String fileName;
    private Long fileSize;
    private Integer enabled;
    private Integer publishStatus;
}
