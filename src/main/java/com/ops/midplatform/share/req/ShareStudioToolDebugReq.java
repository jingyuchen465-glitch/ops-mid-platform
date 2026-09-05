package com.ops.midplatform.share.req;

import lombok.Data;

import java.util.Map;

/**
 * 创作空间调试动态 Tool 的请求。
 */
@Data
public class ShareStudioToolDebugReq {

    /**
     * 本次调试传给 Groovy 脚本的参数。
     *
     * <p>脚本中通过 params 变量读取这些参数。</p>
     */
    private Map<String, Object> params;

    /**
     * 未保存工具临时调试时传入的工具配置。
     *
     * <p>调试已保存工具时可以不传，后端会从数据库读取配置。</p>
     */
    private ShareStudioToolSaveReq tool;
}
