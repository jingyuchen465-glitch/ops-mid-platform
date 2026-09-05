package com.ops.midplatform.share.res;

import lombok.Data;

/**
 * 创作空间动态 Tool 调试响应。
 */
@Data
public class ShareStudioToolDebugRes {

    /**
     * 调试是否成功。
     */
    private Boolean success;

    /**
     * Groovy 脚本 return 的结果。
     */
    private Object result;

    /**
     * 错误信息。
     */
    private String errorMessage;

    /**
     * 调试耗时，单位毫秒。
     */
    private Long durationMs;
}
