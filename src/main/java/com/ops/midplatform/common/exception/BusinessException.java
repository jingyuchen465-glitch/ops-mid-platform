package com.ops.midplatform.common.exception;

import lombok.Getter;

/** 可预期的业务错误，由全局异常处理器转换为统一响应。 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
