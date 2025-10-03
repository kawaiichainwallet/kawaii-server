package com.kawaiichainwallet.common.core.exception;

import com.kawaiichainwallet.common.core.enums.ApiCode;

import java.io.Serial;

/**
 * 限流异常
 */
public class RateLimitException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RateLimitException(String message) {
        super(ApiCode.TOO_MANY_REQUESTS, message);
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public RateLimitException(ApiCode apiCode) {
        super(apiCode);
    }

    public RateLimitException(ApiCode apiCode, String message) {
        super(apiCode, message);
    }
}
