package com.ops.midplatform.admin.req;

import lombok.Data;

import java.util.List;

/** 保存 Token Prompt 选择的请求。 */
@Data
public class AdminTokenPromptSelectionSaveReq {
    /** 当前 Token 选择的 Prompt 列表。 */
    private List<AdminTokenPromptSelectionItemReq> prompts;
}
