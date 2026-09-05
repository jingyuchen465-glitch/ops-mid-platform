package com.ops.midplatform.admin.res;

import lombok.Data;

/** Token Resource 选择展示对象。 */
@Data
public class AdminTokenResourceSelectionRes {
    private Long id;
    private Long tokenId;
    private String resourceUri;
    private Integer enabled;
}
