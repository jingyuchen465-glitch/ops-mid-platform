package com.ops.midplatform.share.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Skill Markdown 文件上传预签名请求。 */
@Data
public class ShareStudioSkillPresignReq {
    private String skillCode;

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    private Long fileSize;
}
