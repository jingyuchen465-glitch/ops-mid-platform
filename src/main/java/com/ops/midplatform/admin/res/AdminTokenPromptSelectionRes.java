package com.ops.midplatform.admin.res;

import lombok.Data;

/** Token Prompt 选择展示对象。 */
@Data
public class AdminTokenPromptSelectionRes {
    private Long id;
    private Long tokenId;
    private String promptName;
    private Integer enabled;
}
