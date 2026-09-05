package com.ops.midplatform.share.req;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 创作空间未保存 API 的临时调试请求。
 *
 * <p>用户在创作页面填写 HTTP API 配置后，应该先点击“发送”验证接口是否可用。
 * 只有调试结果符合预期，再点击“保存”把配置落库。因此这个请求会携带完整
 * API 表单数据和本次调试参数，但不会创建 mcp_request_config 记录。</p>
 *
 * <p>它继承 ShareStudioApiSaveReq，是为了复用 API 表单字段；额外增加 params，
 * 用来表达“这一次点击发送时传入什么参数”。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShareStudioApiTemporaryDebugReq extends ShareStudioApiSaveReq {

    /**
     * 本次调试参数。
     *
     * <p>它只影响当前这一次调试，不会保存到数据库。
     * 如果想保存默认值，应该填写父类里的 paramsDefault。</p>
     */
    private Map<String, Object> params;
}
