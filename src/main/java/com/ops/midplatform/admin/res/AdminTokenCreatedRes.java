package com.ops.midplatform.admin.res;

import lombok.Data;

/** 管理端创建 Token 后返回对象。 */
@Data
public class AdminTokenCreatedRes {
    /** Token 数据。 */
    private AdminTokenRes token;

    /** 只在创建成功时展示一次的明文 Token。 */
    private String rawToken;
}
