package com.bear.mcp.single.share.req;

import lombok.Data;

import java.util.Map;

/**
 * 已保存 API 的调试请求。
 *
 * <p>列表里点“调试”时，API 的 URL、Header、Body 模板都已经在数据库里，
 * 这个 Req 只需要传本次调试要使用的参数。</p>
 */
@Data
public class ShareStudioApiDebugReq {

    /**
     * 本次调试参数。
     *
     * <p>例如 URL 是 /course?pageNum={{pageNum}}，这里传入 pageNum=2，
     * 请求执行时就会把占位符替换成本次调试值。</p>
     */
    private Map<String, Object> params;
}
