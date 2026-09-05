package com.bear.mcp.single.share.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 创作空间保存 Skill 请求。 */
@Data
public class ShareStudioSkillSaveReq {
    private String skillCode;

    @NotBlank(message = "Skill名称不能为空")
    private String name;

    private String description;
    private String category;

    @NotBlank(message = "请先上传 Markdown 文件")
    private String objectKey;

    private String fileName;
    private Long fileSize;
    private Integer enabled;
    private Integer publishStatus;
}
